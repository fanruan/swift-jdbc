/**
 * Created by Young's on 2016/3/8.
 * 带有序号的TableTree
 */
BI.TableTreeWithNumber = BI.inherit(BI.Widget, {
    _defaultConfig: function () {
        return BI.extend(BI.TableTreeWithNumber.superclass._defaultConfig.apply(this, arguments), {
            baseCls: "bi-number-table-tree",

            isNeedResize: false,//是否需要调整列宽
            isResizeAdapt: true,//是否需要在调整列宽或区域宽度的时候它们自适应变化

            isNeedFreeze: false,//是否需要冻结单元格
            freezeCols: [], //冻结的列号,从0开始,isNeedFreeze为tree时生效

            isNeedMerge: true,//是否需要合并单元格
            mergeCols: [],
            mergeRule: function (row1, row2) { //合并规则, 默认相等时合并
                return BI.isEqual(row1, row2);
            },

            columnSize: [],
            headerRowSize: 37,
            footerRowSize: 37,
            rowSize: 37,

            regionColumnSize: false,

            header: [],
            footer: false,
            items: [],

            //交叉表头
            crossHeader: [],
            crossItems: [],

            showNumber: true
        })
    },

    _createHeader: function (deep, vDeep) {
        var self = this, o = this.options;
        var header = o.header || [], crossHeader = o.crossHeader || [], showNumber = o.showNumber;
        var items = this.tableTree._formatCrossItems(o.crossItems, vDeep);
        var result = [];
        //序号
        if(showNumber === true){
            deep++;
            header.splice(0, 0, {
                type: "bi.page_table_cell",
                text: BI.i18nText("BI-Number_Index")
            });
        }
        BI.each(items, function (row, node) {
            var c = [];
            BI.count(0, deep, function(){
                c.push(crossHeader[row]);
            });
            result.push(c.concat(node || []));
        });
        result.push(header);
        return result;
    },

    _init: function () {
        BI.TableTreeWithNumber.superclass._init.apply(this, arguments);
        var self = this, o = this.options;
        var mergeCols = o.mergeCols;
        this.tableTree = new BI.TableTree();
        var deep = this.tableTree._getHDeep();
        var vDeep = this.tableTree._getVDeep();
        var header = this._createHeader(deep, vDeep);
        var items = this.tableTree._formatItems(o.items, deep);
        this._formatItemsAndMergeCols(items, mergeCols);

        this.table = BI.createWidget({
            type: "bi.table_view",
            element: this.element,
            logic: o.logic,

            isNeedResize: o.isNeedResize,
            isResizeAdapt: o.isResizeAdapt,

            isNeedFreeze: o.isNeedFreeze,
            freezeCols: o.freezeCols,
            isNeedMerge: o.isNeedMerge,
            mergeCols: mergeCols,
            mergeRule: o.mergeRule,

            columnSize: o.columnSize,
            headerRowSize: o.headerRowSize,
            footerRowSize: o.footerRowSize,
            rowSize: o.rowSize,

            regionColumnSize: o.regionColumnSize,

            header: header,
            footer: o.footer,
            items: items
        });
        this.table.on(BI.Table.EVENT_TABLE_AFTER_INIT, function () {
            self.fireEvent(BI.Table.EVENT_TABLE_AFTER_INIT, arguments);
        });
        this.table.on(BI.Table.EVENT_TABLE_RESIZE, function () {
            self.fireEvent(BI.Table.EVENT_TABLE_RESIZE, arguments);
        });
        this.table.on(BI.Table.EVENT_TABLE_SCROLL, function () {
            self.fireEvent(BI.Table.EVENT_TABLE_SCROLL, arguments);
        });
        this.table.on(BI.Table.EVENT_TABLE_BEFORE_REGION_RESIZE, function () {
            self.fireEvent(BI.Table.EVENT_TABLE_BEFORE_REGION_RESIZE, arguments);
        });
        this.table.on(BI.Table.EVENT_TABLE_REGION_RESIZE, function () {
            self.fireEvent(BI.Table.EVENT_TABLE_REGION_RESIZE, arguments);
        });
        this.table.on(BI.Table.EVENT_TABLE_AFTER_REGION_RESIZE, function () {
            self.fireEvent(BI.Table.EVENT_TABLE_AFTER_REGION_RESIZE, arguments);
        });
        this.table.on(BI.Table.EVENT_TABLE_BEFORE_COLUMN_RESIZE, function () {
            self.fireEvent(BI.Table.EVENT_TABLE_BEFORE_COLUMN_RESIZE, arguments);
        });
        this.table.on(BI.Table.EVENT_TABLE_COLUMN_RESIZE, function () {
            self.fireEvent(BI.Table.EVENT_TABLE_COLUMN_RESIZE, arguments);
        });
        this.table.on(BI.Table.EVENT_TABLE_AFTER_COLUMN_RESIZE, function () {
            self.fireEvent(BI.Table.EVENT_TABLE_AFTER_COLUMN_RESIZE, arguments);
        });
    },

    _formatItemsAndMergeCols: function(items, mergeCols){
        //处理序号 mergeCol也要处理，index向后+1
        var showNumber = this.options.showNumber;
        if(showNumber === true) {
            var count = 0;
            BI.each(items, function(i, item){
                //行 isSum === true 为汇总行
                var sumIndex = -1;
                BI.some(item, function(j, it){
                    if(it.isSum === true){
                        sumIndex = j;
                        return true;
                    }
                });
                if(sumIndex !== -1){
                    item.splice(0, 0, {
                        type: "bi.page_table_cell",
                        text: BI.i18nText("BI-Summary_Values"),
                        tag: item.tag
                    });
                } else {
                    count ++;
                    item.splice(0, 0, {
                        type: "bi.page_table_cell",
                        text: count
                    })
                }
            });
            BI.each(mergeCols, function(i, col){
                mergeCols[i]++;
            });
        }
        BI.isNotNull(this.table) && this.table.attr("mergeCols", mergeCols);
    },

    resize: function () {
        this.table.resize();
    },

    setColumnSize: function (columnSize) {
        this.table.setColumnSize(columnSize);
    },

    getColumnSize: function () {
        return this.table.getColumnSize();
    },

    getCalculateColumnSize: function () {
        return this.table.getCalculateColumnSize();
    },

    setHeaderColumnSize: function (columnSize) {
        this.table.setHeaderColumnSize(columnSize);
    },

    setRegionColumnSize: function (columnSize) {
        this.table.setRegionColumnSize(columnSize);
    },

    getRegionColumnSize: function () {
        return this.table.getRegionColumnSize();
    },

    getCalculateRegionColumnSize: function () {
        return this.table.getCalculateRegionColumnSize();
    },

    getCalculateRegionRowSize: function () {
        return this.table.getCalculateRegionRowSize();
    },

    getClientRegionColumnSize: function () {
        return this.table.getClientRegionColumnSize();
    },

    getScrollRegionColumnSize: function () {
        return this.table.getScrollRegionColumnSize();
    },

    getScrollRegionRowSize: function () {
        return this.table.getScrollRegionRowSize();
    },

    hasVerticalScroll: function () {
        return this.table.hasVerticalScroll();
    },

    setVerticalScroll: function (scrollTop) {
        this.table.setVerticalScroll(scrollTop);
    },

    setLeftHorizontalScroll: function (scrollLeft) {
        this.table.setLeftHorizontalScroll(scrollLeft);
    },

    setRightHorizontalScroll: function (scrollLeft) {
        this.table.setRightHorizontalScroll(scrollLeft);
    },

    getVerticalScroll: function () {
        return this.table.getVerticalScroll();
    },

    getLeftHorizontalScroll: function () {
        return this.table.getLeftHorizontalScroll();
    },

    getRightHorizontalScroll: function () {
        return this.table.getRightHorizontalScroll();
    },

    getColumns: function () {
        return this.table.getColumns();
    },

    attr: function () {
        BI.TableTreeWithNumber.superclass.attr.apply(this, arguments);
        this.table.attr.apply(this.table, arguments);
    },

    populate: function (items, header, crossItems, crossHeader) {
        var o = this.options;
        o.items = items || [];
        if (BI.isNotNull(header)) {
            o.header = header;
        }
        if (BI.isNotNull(crossItems)) {
            o.crossItems = crossItems;
        }
        if (BI.isNotNull(crossHeader)) {
            o.crossHeader = crossHeader;
        }
        var deep = this.tableTree._getHDeep();
        var vDeep = this.tableTree._getVDeep();
        var header = this._createHeader(deep, vDeep);
        items = this.tableTree._formatItems(o.items, deep);
        this._formatItemsAndMergeCols(items, o.mergeCols);
        this.table.populate(items, header);
    },

    destroy: function () {
        this.table.destroy();
        BI.TableTreeWithNumber.superclass.destroy.apply(this, arguments);
    }
});
$.shortcut("bi.table_tree_with_number", BI.TableTreeWithNumber);