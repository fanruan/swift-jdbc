import mixin from 'react-mixin'
import ReactDOM from 'react-dom'
import Immutable from 'immutable'
import {immutableShallowEqual, ReactComponentWithImmutableRenderMixin, requestAnimationFrame} from 'core'
import React, {
    Component,
    StyleSheet,
    Text,
    Dimensions,
    ListView,
    View,
    Fetch,
    Portal
} from 'lib'

import {Sizes, TemplateFactory, WidgetFactory} from 'data'
import {Layout} from 'layout'
import {Table, Dialog, IconLink} from 'base'
import {TableWidget} from 'widgets';

import TableComponent from '../../../../components/Table/TableComponent';

import SettingsContainer from '../Settings/SettingsContainer'


class TableContainer extends Component {
    static contextTypes = {
        $template: React.PropTypes.object,
        actions: React.PropTypes.object
    };

    constructor(props, context) {
        super(props, context);
    }

    componentWillMount() {

    }

    componentDidMount() {

    }

    componentWillUpdate() {

    }

    _renderHeader() {
        const {$widget, wId} = this.props;
        const widget = WidgetFactory.createWidget($widget, wId, TemplateFactory.createTemplate(this.context.$template));
        return <Layout main='justify' cross='center' style={styles.header}>
            <Text>{widget.getName()}</Text>
            <IconLink className='setting-font' onPress={()=> {
                Portal.showModal('TableComponent', <SettingsContainer
                    key={'TableComponent'}
                    $widget={this.props.$widget}
                    wId={this.props.wId}
                    height={0}
                    onComplete={(opt)=> {
                        Portal.closeModal('TableComponent');
                        this.context.actions.updateWidget(opt.$widget, opt.wId);
                    }}
                    onReturn={()=> {
                        Portal.closeModal('TableComponent');
                    }}
                />);
            }}/>
        </Layout>
    }

    render() {
        const {width, height, $widget, wId} = this.props;
        return <Layout dir='top' box='fist'>
            {this._renderHeader()}
            <TableComponent
                width={width}
                height={height - Sizes.HEADER_HEIGHT}
                $widget={$widget}
                wId={wId}
            >
            </TableComponent>
        </Layout>
    }
}
mixin.onClass(TableContainer, ReactComponentWithImmutableRenderMixin);

const styles = StyleSheet.create({
    wrapper: {
        position: 'relative'
    },
    header: {
        paddingLeft: 4,
        paddingRight: 4,
        height: Sizes.HEADER_HEIGHT
    }
});
export default TableContainer
