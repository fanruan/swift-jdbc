BI.Utils = BI.Utils || {};

BI.extend(BI.Utils, {
    afterSaveSQLTable : function(res){
        BI.each(res, function(i, item){
            BI.extend(Pool[i], item);
        })
        BI.Broadcasts.send(BICst.BROADCAST.PACKAGE_PREFIX);
    },

    afterReNameSQLTable : function (id, name, title) {
        Pool["translations"][id] = name;
        BI.some(Pool["packages"][SQLCst.PACK_ID]['tables'], function (idx, item) {
            if(item.id === id) {
                item.describe = title
                return true;
            }
        })
        BI.Broadcasts.send(BICst.BROADCAST.PACKAGE_PREFIX);
    },

    afterDeleteSQLTable : function (id) {
        delete Pool["tables"][id];
        BI.remove(Pool["packages"][SQLCst.PACK_ID]['tables'], function(i, item){
            return item.id === id
        })
        BI.Broadcasts.send(BICst.BROADCAST.PACKAGE_PREFIX);
    },

    getSQLTableDescribe : function (id) {
        if (BI.isNotEmptyArray(BI.Utils.getFieldIDsOfTableID(id))){
            var table =  BI.find(Pool["packages"][SQLCst.PACK_ID]['tables'], function(i, item){
                return item.id === id
            })
            return table.describe;
        } else {
            return BI.i18nText('BI-ETL_Temp_Table_Go_On_Editing')
        }
    },

    getAllSQLTableNames : function (id) {
        var names = [];
        if (BI.isNull(Pool["packages"][SQLCst.PACK_ID])){
            return names;
        }
        BI.each(Pool["packages"][SQLCst.PACK_ID]['tables'], function(i, item){
            if(item.id !== id) {
                names.push(Pool["translations"][item.id])
            }
        })
        return names;
    },
    getSQLTableTypeByID :function (tableId){
        var source = Pool.tables;
        var table = source[tableId];
        if(!table){
            return BICst.BUSINESS_TABLE_TYPE.NORMAL;
        }
        var key = BICst.JSON_KEYS.TABLE_TYPE;
        if(table[key] === undefined || table[key] === null){
            return SQLCst.BUSINESS_TABLE_TYPE.ANALYSIS_SQL_TYPE;
        }
        return table[key];
    },

    getSQLFieldClass: function (type) {
        switch (type) {
            case BICst.COLUMN.STRING:
                return "select-data-field-string-font";
            case BICst.COLUMN.NUMBER:
                return "select-data-field-number-font";
            case BICst.COLUMN.DATE:
                return "select-data-field-date-font";
            default :
                return BI.Utils.getSQLFieldClass(BICst.COLUMN.STRING)
        }
    },

    createSQLDistinctName : function (array, name) {
        var res = name;
        var index = 1;
        while(BI.indexOf(array, res) > -1){
            res = name + index++;
        }
        return res;
    },

    getSQLFieldArrayFromTable : function (table) {
        var fields = [];
        BI.each(table[SQLCst.FIELDS], function (idx, item) {
            fields = BI.concat(fields, item);
        })
        return fields;
    },

    /**
     * 返回数组对象
     * @param tableIds 数组
     * @returns 数组
     */
    getSQLSameConnectionTables: function (tableIds) {
        if(BI.isNull(tableIds) || tableIds.length === 0) {
            //不禁用
            return [];
        }
        var fTable = tableIds[0];
        BI.each(tableIds, function (idx, item) {
            var relation = BI.Utils.getPathsFromTableAToTableB(item, fTable);
            if(relation.length === 0) {
                fTable = item;
            }
        });
        var pTables = Pool.foreignRelations[fTable]
        var result = {};
        BI.each(pTables, function (idx, item) {
            if(item.length === 1) {
                result[idx] = true;
            }
        })
        var fTables = Pool.relations[fTable]
        BI.each(fTables, function (idx, item) {
            if(item.length === 1) {
                result[idx] = true;
            }
        })
        result[fTable] = true;
        return BI.map(result, function (idx, item) {
            return idx;
        })
    },

    getTextFromFormulaValue: function (formulaValue, fieldItems) {
        if (BI.isNull(formulaValue) || BI.isNull(fieldItems)){
            return '';
        }
        var formulaString = "";
        var regx = /\$[\{][^\}]*[\}]|\w*\w|\$\{[^\$\(\)\+\-\*\/)\$,]*\w\}|\$\{[^\$\(\)\+\-\*\/]*\w\}|\$\{[^\$\(\)\+\-\*\/]*[\u4e00-\u9fa5]\}|\w|(.)/g;
        var result = formulaValue.match(regx);
        BI.each(result, function (i, item) {
            var fieldRegx = /\$[\{][^\}]*[\}]/;
            var str = item.match(fieldRegx);
            if (BI.isNotEmptyArray(str)) {
                var id = str[0].substring(2, item.length - 1);
                var item = BI.find(fieldItems, function (i, item) {
                    return id === item.value;
                });
                formulaString = formulaString + BI.isNull(item) ? id : item.text;
            } else {
                formulaString = formulaString + item;
            }
        });
        return formulaString;
    },

    getFieldsFromFormulaValue: function (formulaValue) {
        var fields = [];
        if (BI.isNull(formulaValue)){
            return [];
        }
        var regx = /\$[\{][^\}]*[\}]|\w*\w|\$\{[^\$\(\)\+\-\*\/)\$,]*\w\}|\$\{[^\$\(\)\+\-\*\/]*\w\}|\$\{[^\$\(\)\+\-\*\/]*[\u4e00-\u9fa5]\}|\w|(.)/g;
        var result = formulaValue.match(regx);
        BI.each(result, function (i, item) {
            var fieldRegx = /\$[\{][^\}]*[\}]/;
            var str = item.match(fieldRegx);
            if (BI.isNotEmptyArray(str)) {
                fields.push(str[0].substring(2, item.length - 1));
            }
        });
        return fields;
    },

    createDateFieldType: function (group) {
        switch (group) {
            case BICst.GROUP.Y :
                return BICst.COLUMN.NUMBER;
            case BICst.GROUP.S :
                return BICst.COLUMN.NUMBER;
            case BICst.GROUP.M :
                return BICst.COLUMN.NUMBER;
            case BICst.GROUP.W :
                return BICst.COLUMN.NUMBER;
            case BICst.GROUP.YMD :
                return BICst.COLUMN.DATE;
        }
    },


    buildData : function(model, widget, callback, filterValueGetter) {
        //测试数据
        var header = [];
        var table = {};
        table[SQLCst.ITEMS] = [model];
        var mask = BI.createWidget({
                type: "bi.etl_loading_mask",
                masker: widget.element,
                text: BI.i18nText("BI-Loading")
            });

        BI.ETLReq.reqPreviewTable(table, function (data) {
            BI.each(model[SQLCst.FIELDS], function(idx, item){
                var head = {
                    text:item.field_name,
                    field_type:item.field_type,
                    field_id:item.field_id,
                    filterValueGetter : filterValueGetter
                }
                head[SQLCst.FIELDS] = model[SQLCst.FIELDS]
                header.push(head);
            });
            if(mask != null) {
                mask.destroy()
            }
            callback([data.value, header])
        });

    },

    triggerPreview : function () {
        return BI.throttle(function () {
            if(BI.isNull(this.runner)){
                this.runner = new BI.SQLRUN({
                    args : arguments
                })
                var self = this;
                var run = function (widget, previewModel, operatorType, type) {
                    switch (type) {
                        case SQLCst.PREVIEW.SELECT :
                        {
                            BI.Utils.buildData(previewModel.update4Preview(), widget.previewTable, function (data) {
                                self.runner = self.runner.getNext();
                                if (self.runner != null) {
                                    self.runner.submit(run)
                                } else {
                                    widget.setPreviewOperator(operatorType);
                                    widget.populatePreview.apply(widget, data)
                                }
                            }, widget.controller.getFilterValue)
                            return
                        }
                        case  SQLCst.PREVIEW.MERGE :
                        {
                            BI.concat(BI.Utils.buildData(previewModel, widget, function (data) {
                                self.runner = self.runner.getNext();
                                if (self.runner != null) {
                                    self.runner.submit(run)
                                } else {
                                    widget.populate.apply(widget, data);
                                }
                            }), operatorType);
                            return;
                        }
                        default :
                        {
                            BI.Utils.buildData(previewModel.update(), widget.previewTable, function (data) {
                                self.runner = self.runner.getNext();
                                if (self.runner != null) {
                                    self.runner.submit(run)
                                } else {
                                    widget.setPreviewOperator(operatorType);
                                    widget.populatePreview.apply(widget, data)
                                }
                            }, widget.controller.getFilterValue);
                            return
                        }

                    }
                }
                self.runner.submit(run)
            } else {
                this.runner.setNext({
                    args : arguments
                })
            }
        }, 300)
    }

})

BI.SQLRUN = BI.inherit(FR.OB, {
    _init : function () {
        BI.SQLRUN.superclass._init.apply(this, arguments);
    },

    setNext : function (next) {
        this.next = new BI.SQLRUN(next);
    },

    hasNext : function () {
        return BI.isNotNull(this.next);
    },

    submit : function (runner) {
        runner.apply(runner, this.options.args)
    },

    getNext : function () {
        return this.next
    }
})

