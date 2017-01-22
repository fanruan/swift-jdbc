/**
 * @class BIDezi.StringDetailView
 * @extends BI.View
 *
 */
BIDezi.StringDetailView = BI.inherit(BI.View, {

    constants: {
        DETAIL_NORTH_HEIGHT: 40,
        DETAIL_TAB_WIDTH: 100,
        DETAIL_TAB_HEIGHT: 40,
        DETAIL_WEST_WIDTH: 280,
        DETAIL_DATA_STYLE_HEIGHT: 320,
        DETAIL_GAP_NORMAL: 10,
        DETAIL_PANE_HORIZONTAL_GAP: 10,
        DETAIL_TITLE_WIDTH: 400,
        DETAIL_TITLE_HEIGHT: 22
    },

    _defaultConfig: function () {
        return BI.extend(BIDezi.StringDetailView.superclass._defaultConfig.apply(this, arguments), {
            baseCls: "bi-widget-attribute-setter"
        })
    },

    _init: function () {
        BIDezi.StringDetailView.superclass._init.apply(this, arguments);
    },

    _render: function (vessel) {
        var mask = BI.createWidget();
        mask.element.__buildZIndexMask__(0);
        var west = this._buildWest();
        var items = [{
            el: west,
            width: this.constants.DETAIL_WEST_WIDTH
        }, {
            type: "bi.vtape",
            items: [{
                el: this._buildNorth(), height: this.constants.DETAIL_NORTH_HEIGHT
            }, {
                el: this._buildCenter()
            }]
        }];
        var htape = BI.createWidget({
            type: "bi.htape",
            cls: "widget-attribute-setter-container",
            items: items
        });
        west.element.resizable({
            handles: "e",
            minWidth: 200,
            maxWidth: 400,
            autoHide: true,
            helper: "bi-resizer",
            start: function () {
            },
            resize: function (e, ui) {
            },
            stop: function (e, ui) {
                items[0].width = ui.size.width;
                htape.resize();
            }
        });
        BI.createWidget({
            type: "bi.absolute",
            element: vessel,
            items: [{
                el: mask,
                left: 0,
                right: 0,
                top: 0,
                bottom: 0
            }, {
                el: htape,
                left: 20,
                right: 20,
                top: 20,
                bottom: 20
            }]
        });
    },

    _buildNorth: function () {
        var self = this;
        this.title = BI.createWidget({
            type: "bi.label",
            textAlign: "left",
            cls: "widget-top-name",
            height: 25,
            text: this.model.get("name")
        });
        var complete = BI.createWidget({
            type: "bi.button",
            height: 25,
            title: BI.i18nText('BI-Return_To_Dashboard'),
            text: BI.i18nText('BI-Detail_Set_Complete')
        });

        complete.on(BI.Button.EVENT_CHANGE, function () {
            self.notifyParentEnd(true);
        });

        return BI.createWidget({
            type: "bi.left_right_vertical_adapt",
            items: {
                left: [this.title],
                right: [complete]
            },
            lhgap: this.constants.DETAIL_PANE_HORIZONTAL_GAP,
            rhgap: this.constants.DETAIL_PANE_HORIZONTAL_GAP
        });
    },

    _buildWest: function () {
        return BI.createWidget({
            type: "bi.absolute",
            items: [{
                el: {
                    type: "bi.select_string",
                    wId: this.model.get("id"),
                    cls: "widget-select-data-pane"
                },
                left: this.constants.DETAIL_PANE_HORIZONTAL_GAP,
                top: this.constants.DETAIL_GAP_NORMAL,
                bottom: this.constants.DETAIL_GAP_NORMAL,
                right: this.constants.DETAIL_PANE_HORIZONTAL_GAP
            }],
            cls: "widget-attr-west"
        });
    },

    _buildCenter: function () {
        var combo = this._createCombo();

        var top = BI.createWidget({
            type: "bi.vtape",
            cls: "widget-top-wrapper",
            items: [{
                el: {
                    type: "bi.button_group",
                    items: BI.createItems([{
                        text: BI.i18nText("BI-Data"),
                        selected: true
                    }], {
                        type: "bi.line_segment_button",
                        height: this.constants.DETAIL_TAB_HEIGHT
                    }),
                    height: this.constants.DETAIL_TAB_HEIGHT,
                    layouts: [{
                        type: "bi.absolute_center_adapt",
                        items: [{
                            type: "bi.center",
                            width: this.constants.DETAIL_TAB_WIDTH,
                            height: this.constants.DETAIL_TAB_HEIGHT
                        }]
                    }]
                },
                height: this.constants.DETAIL_TAB_HEIGHT
            }, {
                el: this._createRegion()
            }]
        });

        return BI.createWidget({
            type: "bi.absolute",
            cls: "widget-attr-center",
            items: [{
                el: {
                    type: "bi.vtape",
                    items: [{
                        el: top,
                        height: this.constants.DETAIL_DATA_STYLE_HEIGHT - this.constants.DETAIL_NORTH_HEIGHT
                    }, {
                        el: {
                            type: "bi.absolute",
                            cls: "widget-center-wrapper",
                            items: [{
                                el: combo,
                                left: 10,
                                right: 10,
                                top: 10
                            }]
                        }
                    }],
                    vgap: 10
                },
                left: 0,
                right: this.constants.DETAIL_PANE_HORIZONTAL_GAP,
                top: 0,
                bottom: this.constants.DETAIL_GAP_NORMAL
            }]
        });
    },

    _createRegion: function () {
        var self = this;
        var dimensionsVessel = {};
        this.dimensionsManager = BI.createWidget({
            type: "bi.string_dimensions_manager",
            wId: this.model.get("id"),
            dimensionCreator: function (dId, regionType, op) {
                if (!dimensionsVessel[dId]) {
                    dimensionsVessel[dId] = BI.createWidget({
                        type: "bi.layout"
                    });
                    self.addSubVessel(dId, dimensionsVessel[dId]);
                    var dimensions = self.model.cat("dimensions");
                    if (!BI.has(dimensions, dId)) {
                        self.model.set("addDimension", {
                            dId: dId,
                            regionType: regionType,
                            src: op
                        });
                    }
                }
                return dimensionsVessel[dId];
            }
        });

        this.dimensionsManager.on(BI.StringDimensionsManager.EVENT_CHANGE, function () {
        });
        return this.dimensionsManager;
    },

    _createCombo: function () {
        var self = this;
        this.combo = BI.createWidget({
            type: "bi.select_data_combo",
            wId: this.model.get("id")
        });
        this.combo.on(BI.SelectDataCombo.EVENT_CONFIRM, function () {
            self.model.set("value", this.getValue());
        });
        return this.combo;
    },

    splice: function (old, key1, key2) {
        if (key1 === "dimensions") {
            this.dimensionsManager.populate();
            this.combo.setValue();
            this.model.set("value", this.combo.getValue());
        }
    },

    local: function () {
        if (this.model.has("addDimension")) {
            this.model.get("addDimension");
            return true;
        }
        return false;
    },

    change: function (changed, prev) {
        if (BI.has(changed, "dimensions")) {
            this.combo.setValue();
        }
        if (BI.has(changed, "dimensions") || BI.has(changed, "view")) {
            this.dimensionsManager.populate();
        }
        if (BI.has(changed, "dimensions")) {
            this._refreshDimensions();
            this._checkDataBind();
        }
    },

    _checkDataBind: function () {
        if (BI.size(this.model.get("dimensions")) > 0) {
            this.combo.setEnable(true);
        } else {
            this.combo.setEnable(false);
        }
    },

    _refreshDimensions: function () {
        var self = this;
        BI.each(self.model.cat("view"), function (regionType, dids) {
            BI.each(dids, function (i, dId) {
                self.skipTo(regionType + "/" + dId, dId, "dimensions." + dId);
            });
        });
    },

    refresh: function () {
        var self = this;
        this.dimensionsManager.populate();
        this._refreshDimensions();
        this._checkDataBind();
        this.combo.setValue(this.model.get("value"));
    }
});