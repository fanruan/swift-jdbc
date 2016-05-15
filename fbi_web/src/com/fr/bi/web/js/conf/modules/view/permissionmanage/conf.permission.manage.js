/**
 * Created by wuk on 16/4/18.
 */
BIConf.PermissionManageView = BI.inherit(BI.View, {

    _defaultConfig: function () {
        return BI.extend(BIConf.PermissionManageView.superclass._defaultConfig.apply(this, arguments), {
            baseCls: ""
        })
    },
    _init: function () {
        BIConf.PermissionManageView.superclass._init.apply(this, arguments);
    },

    _render: function (vessel) {
        var self=this;
        this.main = BI.createWidget({
            type: "bi.border",
            element: vessel,
            items: {
                west: {el: this._builtPackageTree(), width: 400},
                center: {el: this._buildAuthorityPane()}
            }
        });
        self.set('isShow','0');
    },
    change: function (changed) {
        var self = this;
        alert(self.get('isShow'));
        if (changed.isShow){
        }
    },
    load: function () {
    },

    local: function () {
        return false;
    },
    refresh: function () {
    },

    _builtPackageTree: function () {
        var self = this;
        this.packageTree = BI.createWidget({
            type: "bi.package_authority_tree"
        });
        /*单选模式下,直接点击某个业务包,在右侧权限管理页面,否则显示初始页面,初始页面分批量和单选两种*/
        // this.packageTree.on(BI.PackageAndAuthorityTree.EVENT_TYPE_CHANGE, function () {
        //     self._setHeadTitle(JSON.parse(self.packageTree.getPackageIds()), self.packageTree.getSelectType());
        //     // self.skipTo("init/show", "pane", '');
        // self.set('allRoles','111');
        // });
        // this.packageTree.on(BI.PackageAndAuthorityTree.EVENT_SELECT_CHANGE, function () {
        //     self.model.set('packageIds', JSON.parse(self.packageTree.getPackageIds()));
        //     self._setHeadTitle(JSON.parse(self.packageTree.getPackageIds()), self.packageTree.getSelectType());
        // })
        this.packageTree.on(BI.PackageAndAuthorityTree.EVENT_CHANGE, function () {
            self.model.set('packageIds', JSON.parse(self.packageTree.getPackageIds()));
            self.model.set('selectType',self.packageTree.getSelectType());
            alert(self.model.get('isShow'));
            self.model.set('isShow','1');
            BI.Layers.show("layer");
            self._setHeadTitle(JSON.parse(self.packageTree.getPackageIds()), self.packageTree.getSelectType());
        })
        return this.packageTree;
    },
    _buildAuthorityPane: function () {
        var self=this;
        this.authorityPaneRoleMain.createWidget({
            type:'bi.authority_pane_role_main'
        });
        return BI.createWidget({
            type: "bi.border",
            items: {
                north: {el: this._showTitle(), height: 40},
                center: {
                    el: BI.Layers.create("layer",self.authorityPaneRoleMain),
                    top: 0,
                    left: 0,
                    right: 0,
                    bottom: -40
                }
            }
        })
    },


    _showTitle: function () {
        this.title = BI.createWidget({
            type: "bi.label",
            text: BI.i18nText('BI-Permissions_Setting')
        });
        return this.title;
    },
    //设置标题
    _setHeadTitle: function (packageId,selectType) {
        var self = this;
        switch (selectType){
            case BI.PackageAndAuthorityTree.SelectType.SingleSelect:
                self.title.setText(packageId.length != 0 ? BI.Utils.getPackageNameByID4Conf(packageId[0]) : '' + BI.i18nText('BI-Permissions_Setting'));
                break;
            case BI.PackageAndAuthorityTree.SelectType.MultiSelect:
                self.title.setText(BI.i18nText('BI-Permissions_Setting') + '配置   ' + packageId.length + '个业务包');
                break;
        }
    },
});
