/**
 * 带有序号的表格
 *
 * Created by GUY on 2016/5/26.
 * @class BI.StyleTable
 * @extends BI.Widget
 */
BI.StyleTable = BI.inherit(BI.Widget, {

    _defaultConfig: function () {
        return BI.extend(BI.StyleTable.superclass._defaultConfig.apply(this, arguments), {
            baseCls: "bi-style-table",
            el: {
                type: "bi.page_table"
            },

            color: null,
            style: null,

            isNeedResize: true,
            isResizeAdapt: false,

            isNeedFreeze: false,//是否需要冻结单元格
            freezeCols: [], //冻结的列号,从0开始,isNeedFreeze为true时生效

            isNeedMerge: false,//是否需要合并单元格
            mergeCols: [], //合并的单元格列号
            mergeRule: function (row1, row2) { //合并规则, 默认相等时合并
                return BI.isEqual(row1, row2);
            },

            columnSize: [],
            headerRowSize: 25,
            footerRowSize: 25,
            rowSize: 25,

            regionColumnSize: false,

            header: [],
            footer: false,
            items: [], //二维数组

            //交叉表头
            crossHeader: [],
            crossItems: []
        });
    },

    _init: function () {
        BI.StyleTable.superclass._init.apply(this, arguments);
        var self = this, o = this.options;

        this.table = BI.createWidget(o.el, {
            type: "bi.page_table",
            element: this.element,
            isNeedResize: o.isNeedResize,
            isResizeAdapt: o.isResizeAdapt,

            isNeedFreeze: o.isNeedFreeze,
            freezeCols: o.freezeCols,

            isNeedMerge: o.isNeedMerge,
            mergeCols: o.mergeCols,
            mergeRule: o.mergeRule,

            columnSize: o.columnSize,
            headerRowSize: o.headerRowSize,
            footerRowSize: o.footerRowSize,
            rowSize: o.rowSize,

            regionColumnSize: o.regionColumnSize,

            header: o.header,
            footer: o.footer,
            items: o.items,
            //交叉表头
            crossHeader: o.crossHeader,
            crossItems: o.crossItems
        });

        this.table.on(BI.Table.EVENT_TABLE_AFTER_INIT, function () {
            self.fireEvent(BI.StyleTable.EVENT_TABLE_AFTER_INIT);
        });
        this.table.on(BI.Table.EVENT_TABLE_AFTER_REGION_RESIZE, function () {
            self.fireEvent(BI.StyleTable.EVENT_TABLE_AFTER_REGION_RESIZE);
        });
        this.table.on(BI.Table.EVENT_TABLE_AFTER_COLUMN_RESIZE, function () {
            self.fireEvent(BI.StyleTable.EVENT_TABLE_AFTER_COLUMN_RESIZE);
        });

        if (BI.isKey(o.color)) {
            this.setColor(o.color);
        }
        if (BI.isKey(o.style)) {
            this.setStyle(o.style);
        }
    },

    resize: function () {
        this.table.resize();
    },

    setColumnSize: function (size) {
        this.table.setColumnSize(size);
    },

    getColumnSize: function () {
        return this.table.getColumnSize();
    },

    getCalculateColumnSize: function () {
        return this.table.getCalculateColumnSize();
    },

    getCalculateRegionColumnSize: function () {
        return this.table.getCalculateRegionColumnSize();
    },

    setVPage: function (v) {
        this.table.setVPage(v);
    },

    getVPage: function () {
        return this.table.getVPage();
    },

    setHPage: function (v) {
        this.table.setHPage(v);
    },

    getHPage: function () {
        return this.table.getHPage();
    },

    attr: function () {
        BI.StyleTable.superclass.attr.apply(this, arguments);
        this.table.attr.apply(this.table, arguments);
    },

    showSequence: function () {
        this.table.showSequence();
    },

    hideSequence: function () {
        this.table.hideSequence();
    },

    _parseHEXAlpha2HEX: function (hex, alpha) {
        var rgb = BI.DOM.hex2rgb(hex);
        var rgbJSON = BI.DOM.rgb2json(rgb);
        rgbJSON.a = alpha;
        //return BI.DOM.json2rgba(rgbJSON);
        return BI.DOM.rgba2rgb(BI.DOM.json2rgba(rgbJSON));
    },

    setColor: function (color) {
        this.options.color = color;
        this.setStyleAndColor(this.options.style, color);
    },

    refresh: function() {
        this.table.refresh.apply(this.table, arguments);
        this.setStyleAndColor(this.options.style, this.options.color);
    },

    populate: function (items) {
        this.table.populate.apply(this.table, arguments);
        this.setStyleAndColor(this.options.style, this.options.color);
    },

    destroy: function () {
        this.table.destroy();
        BI.StyleTable.superclass.destroy.apply(this, arguments);
    },

    setStyleAndColor: function (style, color) {
        if(BI.isNull(style) || BI.isNull(color)) {
            return;
        }
        var $table = this.table.element;
        var $topLeft = $table.find(".scroll-top-left .table"), $topRight = $table.find(".scroll-top-right .table"),
            $bottomLeft = $table.find(".scroll-bottom-left .table"), $bottomRight = $table.find(".scroll-bottom-right .table"),
            $sequenceHeader = $table.find(".sequence-table-title"), $oddSequence = $table.find(".sequence-table-number.odd"),
            $evenSequence = $table.find(".sequence-table-number.even");
        var $bottomLeftSum = $bottomLeft.find(".summary-cell"), $bottomRightSum = $bottomRight.find(">tbody .summary-cell"),
            $bottomLeftSumLast = $bottomLeft.find(".summary-cell.last"), $bottomRightSumLast = $bottomRight.find(">tbody .summary-cell.last"),
            $sequenceSum = $table.find(".sequence-table-summary"), $sequenceSumLast = $table.find(".sequence-table-summary.last");

        var $rowHeader = $table.find(".layer-tree-table-title");
        switch (style) {
            case BI.StyleTable.STYLE1:
                var oddColor = this._parseHEXAlpha2HEX(color, 0.2),
                    evenColor = this._parseHEXAlpha2HEX(color, 0.05),
                    summaryColor = this._parseHEXAlpha2HEX(color, 0.4);

                //background
                $topLeft.css("background", color);
                $topRight.css("background", color);
                $bottomRight.find(">thead > tr").css("background", color);
                $bottomLeft.find("> tbody tr.odd").css("background", oddColor);
                $bottomRight.find("> tbody tr.odd").css("background", oddColor);
                $bottomLeft.find("> tbody tr.even").css("background", evenColor);
                $bottomRight.find("> tbody tr.even").css("background", evenColor);
                $sequenceHeader.css("background", color);
                $oddSequence.css("background", this._parseHEXAlpha2HEX(color, 0.2));
                $evenSequence.css("background", this._parseHEXAlpha2HEX(color, 0.05));
                $bottomLeftSum.css("background", summaryColor);
                $bottomRightSum.css("background", summaryColor);
                $sequenceSum.css("background", summaryColor);
                $bottomLeftSumLast.css("background", color);
                $bottomRightSumLast.css("background", color);
                $sequenceSumLast.css("background", color);

                //color
                $topLeft.css("color", "#ffffff");
                $topRight.css("color", "#ffffff");
                $bottomRight.find("thead > tr").css("color", "#ffffff");
                $sequenceHeader.css("color", "white");
                $bottomLeftSum.css("color", "#1a1a1a");
                $bottomRightSum.css("color", "#1a1a1a");
                $sequenceSum.css("color", "#1a1a1a");
                $bottomLeftSumLast.css("color", "#ffffff");
                $bottomRightSumLast.css("color", "#ffffff");
                $sequenceSumLast.css("color", "#ffffff");

                //font weight
                $bottomLeftSum.css("fontWeight", "bold");
                $bottomRightSum.css("fontWeight", "bold");
                $sequenceSum.css("fontWeight", "bold");
                $bottomLeftSumLast.css("fontWeight", "bold");
                $bottomRightSumLast.css("fontWeight", "bold");
                $sequenceSumLast.css("fontWeight", "bold");
                $rowHeader.css("fontWeight", "bold");

                break;
            case BI.StyleTable.STYLE2:
                //background
                $topLeft.css("background", color);
                $topRight.css("background", color);
                $bottomRight.find(">thead > tr").css("background", color);
                $sequenceHeader.css("background", color);
                $bottomLeftSum.css("background", "#ffffff");
                $bottomRightSum.css("background", "#ffffff");
                $sequenceSum.css("background", "#ffffff");
                $bottomLeftSumLast.css("background", "#ffffff");
                $bottomRightSumLast.css("background", "#ffffff");
                $sequenceSumLast.css("background", "#ffffff");

                //color
                $topLeft.css("color", "#ffffff");
                $topRight.css("color", "#ffffff");
                $bottomRight.find("thead > tr").css("color", "#ffffff");
                $sequenceHeader.css("color", "white");
                $bottomLeftSum.css("color", "#1a1a1a");
                $bottomRightSum.css("color", "#1a1a1a");
                $sequenceSum.css("color", "#1a1a1a");
                $bottomLeftSumLast.css("color", color);
                $bottomRightSumLast.css("color", color);
                $sequenceSumLast.css("color", color);

                //font weight
                $bottomLeftSum.css("fontWeight", "bold");
                $bottomRightSum.css("fontWeight", "bold");
                $sequenceSum.css("fontWeight", "bold");
                $bottomLeftSumLast.css("fontWeight", "bold");
                $bottomRightSumLast.css("fontWeight", "bold");
                $sequenceSumLast.css("fontWeight", "bold");
                $rowHeader.css("fontWeight", "bold");

                break;
            case BI.StyleTable.STYLE3:
                //background
                $topLeft.css("background", "#ffffff");
                $topRight.css("background", "#ffffff");
                $bottomRight.find(">thead > tr").css("background", "#ffffff");
                $sequenceHeader.css("background", "#ffffff");
                $bottomLeftSum.css("background", "#ffffff");
                $bottomRightSum.css("background", "#ffffff");
                $sequenceSum.css("background", "#ffffff");
                $bottomLeftSumLast.css("background", color);
                $bottomRightSumLast.css("background", color);
                $sequenceSumLast.css("background", color);

                //color
                $topLeft.css("color", "#808080");
                $topRight.css("color", "#808080");
                $bottomRight.find("thead > tr").css("color", "#808080");
                $sequenceHeader.css("color", "#808080");
                $bottomLeftSum.css("color", "#1a1a1a");
                $bottomRightSum.css("color", "#1a1a1a");
                $sequenceSum.css("color", "#1a1a1a");
                $bottomLeftSumLast.css("color", "#ffffff");
                $bottomRightSumLast.css("color", "#ffffff");
                $sequenceSumLast.css("color", "#ffffff");

                //font weight
                $bottomLeftSum.css("fontWeight", "bold");
                $bottomRightSum.css("fontWeight", "bold");
                $sequenceSum.css("fontWeight", "bold");
                $bottomLeftSumLast.css("fontWeight", "bold");
                $bottomRightSumLast.css("fontWeight", "bold");
                $sequenceSumLast.css("fontWeight", "bold");
                $rowHeader.css("fontWeight", "bold");

                break;
        }

        //表头
        $table.find(".scroll-bottom-right .table > thead > tr,.sequence-table-title").css({
            fontWeight: "bold"
        });
        $table.find(".scroll-top-left .table .header-cell-text").css({
            fontWeight: "bold"
        });
        $table.find(".scroll-top-right .table").find(" .header-cell-text, .cross-table-target-header, .cross-item-cell, .summary-cell").css({
            fontWeight: "bold"
        });
    },

    setStyle: function (style) {
        this.options.style = style;
        this.setStyleAndColor(style, this.options.color);
    }
});
BI.extend(BI.StyleTable, {
    STYLE1: BICst.TABLE_STYLE.STYLE1,
    STYLE2: BICst.TABLE_STYLE.STYLE2,
    STYLE3: BICst.TABLE_STYLE.STYLE3
});
BI.StyleTable.EVENT_CHANGE = "StyleTable.EVENT_CHANGE";
BI.StyleTable.EVENT_TABLE_AFTER_INIT = "EVENT_TABLE_AFTER_INIT";
BI.StyleTable.EVENT_TABLE_AFTER_COLUMN_RESIZE = "StyleTable.EVENT_TABLE_AFTER_COLUMN_RESIZE";
BI.StyleTable.EVENT_TABLE_AFTER_REGION_RESIZE = "StyleTable.EVENT_TABLE_AFTER_REGION_RESIZE";
$.shortcut('bi.style_table', BI.StyleTable);