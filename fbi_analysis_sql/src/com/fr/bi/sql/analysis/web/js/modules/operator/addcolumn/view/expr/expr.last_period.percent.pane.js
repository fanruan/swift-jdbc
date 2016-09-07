/**
 * Created by 小灰灰 on 2016/4/6.
 */
BI.AnalysisETLOperatorAddColumnExprLastPeriodPercentPane  = BI.inherit(BI.AnalysisETLOperatorAddColumnExprLastPeriodPane, {
    _initController : function() {
        return BI.AnalysisETLOperatorAddColumnExprLastPeriodPercentController;
    },
    _createDetail: function () {
        var self = this;
        self.firstDetail = BI.createWidget({
            type : 'bi.vertical',
            cls : 'detail-view',
            lgap : self._constants.GAP,
            height : self._constants.FIRST_DETAIL_HEIGHT
        });
        self.secondDetail = BI.createWidget({
            type : 'bi.vertical',
            cls : 'detail-view',
            lgap : self._constants.GAP,
            tgap : self._constants.GAP,
            height : self._constants.SECOND_DETAIL_HEIGHT
        });
        self.thrid = BI.createWidget({
            type : 'bi.vertical',
            cls : 'detail-view',
            lgap : self._constants.GAP,
            height : self._constants.FIRST_DETAIL_HEIGHT,
            items : [ BI.createWidget({
                type : 'bi.label',
                cls : 'detail-label',
                textAlign : 'center',
                height : 25,
                text : 1,
                title : 1
            })]
        });
        return BI.createWidget({
            type : 'bi.vertical',
            width : self._constants.DETAIL_WIDTH,
            height : self._constants.LIST_HEIGHT,
            cls :'group-detail',
            scrolly : false,
            items : [
                {
                    el : BI.createWidget({
                        type : 'bi.label',
                        cls : 'label-name',
                        text : BI.i18nText('BI-Group_Detail_Short'),
                        textAlign : 'left',
                        height : self._constants.LABEL_HEIGHT
                    })
                },
                {
                    el : self.firstDetail
                },
                {
                    el : BI.createWidget({
                        type : 'bi.label',
                        cls : 'label-name',
                        text : BI.i18nText('BI-Divide_By'),
                        textAlign : 'center',
                        height : self._constants.LABEL_HEIGHT
                    })
                },
                {
                    el : self.secondDetail
                },
                {
                    el : BI.createWidget({
                        type : 'bi.label',
                        cls : 'label-name',
                        text : BI.i18nText('BI-Minus'),
                        textAlign : 'center',
                        height : self._constants.LABEL_HEIGHT
                    })
                },
                {
                    el : self.thrid
                }
            ]
        });
    }

});
$.shortcut(SQLCst.ANALYSIS_ETL_PAGES.ADD_COLUMN + '_' + BICst.ETL_ADD_COLUMN_TYPE.EXPR_LP_PERCENT, BI.AnalysisETLOperatorAddColumnExprLastPeriodPercentPane);