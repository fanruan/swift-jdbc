/**
 * ContentWidget
 * Created by Young's on 2016/10/12.
 */
import AbstractWidget from './AbstractWidget'

class ContentWidget extends AbstractWidget{
    constructor($widget, ...props) {
        super($widget, ...props);
    }

    //文本组件 内容
    getContent() {
        return this.$widget.get('content');
    }

    //文本组件 样式
    getStyle() {
        return this.$widget.get('style').toJS();
    }
}

export default ContentWidget;