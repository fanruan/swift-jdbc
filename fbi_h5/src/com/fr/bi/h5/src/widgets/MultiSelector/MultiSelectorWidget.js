import PureRenderMixin from 'react-addons-pure-render-mixin'
import mixin from 'react-mixin'
import ReactDOM from 'react-dom'

import {cn, sc, clone, map, requestAnimationFrame, emptyFunction} from 'core'
import React, {
    Component,
    StyleSheet,
    Text,
    Dimensions,
    PixelRatio,
    ListView,
    View,
    Fetch
} from 'lib'

import {TextButton, VirtualScroll, AutoSizer, Infinite, VtapeLayout} from 'base'
import {Colors, Size} from 'data'

import Item from './Item'
import MultiSelectorWidgetHelper from './MultiSelectorWidgetHelper'


class MultiSelectorWidget extends Component {
    constructor(props, context) {
        super(props, context);
        this.state = this._getNextState(props, {
            selected_values: clone(props.value)
        });
    }

    static propTypes = {};

    static defaultProps = {
        type: 0,
        value: [],
        items: [],
        hasNext: false,
        onValueChange: emptyFunction
    };

    state = {};

    _getNextState(props, state = {}) {
        const nextState = {...props, ...state};
        return {
            value: nextState.value,
            selected_values: nextState.selected_values,
            type: nextState.type,
            items: nextState.items,
            hasNext: nextState.hasNext,
            times: nextState.times || 0
        }
    }

    componentWillMount() {

    }

    componentDidMount() {
        this._fetchData();
    }

    _fetchData() {
        if (this.props.itemsCreator) {
            this.props.itemsCreator({
                selected_values: this.state.value,
                times: this.state.times + 1
            }).then((data)=> {
                this.setState(this._getNextState(this.props, {
                    ...this.state, ...{
                        times: this.state.times + 1,
                        hasNext: data.hasNext,
                        items: this.state.items.concat(map(data.value, val=> {
                            return {value: val}
                        }))
                    }
                }));
            })
        }
    }

    componentWillReceiveProps(nextProps) {
        this.setState(this._getNextState(nextProps, {
            times: 0,
            selected_values: clone(nextProps.value)
        }), ()=> {
            this._fetchData();
        });
    }

    componentWillUpdate() {

    }

    render() {
        const {...props} = this.props, {...state} = this.state;
        this._helper = new MultiSelectorWidgetHelper(state);
        //return <Infinite
        //    elementHeight={44}
        //    containerHeight={props.height}
        //    infiniteLoadBeginEdgeOffset={200}
        //    onInfiniteLoad={this.handleInfiniteLoad}
        //    loadingSpinnerDelegate={this.elementInfiniteLoad()}
        //    isInfiniteLoading={true}
        //    timeScrollStateLastsForAfterUserScrolls={1000}
        //    ></Infinite>;
        return <VtapeLayout style={styles.wrapper}>
            <VirtualScroll
                width={props.width}
                height={props.height - Size.ITEM_HEIGHT}
                overscanRowCount={0}
                //noRowsRenderer={this._noRowsRenderer.bind(this)}
                rowCount={this._helper.getSortedItems().length + 1}
                rowHeight={Size.ITEM_HEIGHT}
                rowRenderer={this._rowRenderer.bind(this)}
                //scrollToIndex={scrollToIndex}
            />
            <View height={Size.ITEM_HEIGHT} style={styles.toolbar}>
                <TextButton style={{flex: 1}}
                            onPress={this._onSelectAll.bind(this)}>{state.type === 1 ? '全选' : '全不选'}</TextButton>
            </View>
        </VtapeLayout>;
    }

    _onSelectAll() {
        const type = this.state.type === 2 ? 1 : 2;
        this.setState({
            type: type,
            selected_values: []
        }, ()=> {
            const {selected_values: value, type} = this.state;
            this.props.onValueChange({
                type,
                value
            });
        });
    }

    _moreRenderer() {
        if (this.state.hasNext === true) {
            return <TextButton style={{height: Size.ITEM_HEIGHT}} onPress={()=> {
                this._fetchData();
            }}>点击加载更多数据</TextButton>
        } else {
            return <TextButton style={{height: Size.ITEM_HEIGHT}} disabled={true}>无更多数据</TextButton>
        }
    }

    _rowRenderer({index, isScrolling}) {
        if (index === this._helper.getSortedItems().length) {
            return this._moreRenderer()
        } else {
            const rowData = this._helper.getSortedItems()[index];
            return <Item key={rowData.value} onSelected={(sel)=> {
                if (sel) {
                    this._helper.selectOneValue(rowData.value);
                } else {
                    this._helper.disSelectOneValue(rowData.value);
                }
                this.setState({
                    selected_values: this._helper.getSelectedValue()
                }, ()=> {
                    const {selected_values: value, type} = this.state;
                    this.props.onValueChange({
                        type,
                        value
                    });
                });
            }} {...rowData}/>;
        }
    }
}
mixin.onClass(MultiSelectorWidget, PureRenderMixin);
const styles = StyleSheet.create({
    wrapper: {
        flex: 1
    },
    toolbar: {
        borderTopWidth: 1 / PixelRatio.get(),
        borderTopColor: Colors.BORDER
    }
});
export default MultiSelectorWidget
