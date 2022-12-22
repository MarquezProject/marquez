import React from 'react';
import DataTypeLabel from './DataTypeLabel';
import { toType } from './../../helpers/util';
import splitAndPushByDelimiter from './../../helpers/splitAndPushByDelimiter';

//theme
import Theme from './../../themes/getStyle';

//attribute store for storing collapsed state
import AttributeStore from './../../stores/ObjectAttributes';

export default class extends React.PureComponent {
    constructor(props) {
        super(props);
        this.state = {
            collapsed: AttributeStore.get(
                props.rjvId,
                props.namespace,
                'collapsed',
                true
            )
        };
        this.style = { cursor: 'default' };
    }

    toggleCollapsed = () => {
        this.setState({
            collapsed: !this.state.collapsed
        }, () => {
            AttributeStore.set(
                this.props.rjvId,
                this.props.namespace,
                'collapsed',
                this.state.collapsed
            );
        });
    }

    isLink = (value) => {
        return /(http(s?)):\/\//i.test(value) ? true : false
    }

    getHighlightValue = (value) => {
        let colorLink = Theme(this.props.theme, 'link').style.color;
        let result = splitAndPushByDelimiter(value, this.props.highlightSearch).map((word, i) => [
            <span
                key={i}
                class="string-value"
                style={{ backgroundColor: i % 2 === 1 ? this.props.highlightSearchColor : 'transparent', ...this.style }}
                onClick={this.toggleCollapsed}
            >
                {word}
            </span>
        ])

        if(this.isLink(value)) {
            return <a href={value} style={{ cursor: 'pointer', color: colorLink }} rel="noopener noreferrer" target="_blank">{result}</a>
        }

        return <span>"{result}"</span>
    }

    getValue = (value) => {
        let colorLink = Theme(this.props.theme, 'link').style.color;
        if(this.isLink(value)) {
            return <a href={value} style={{ cursor: 'pointer', color: colorLink }} rel="noopener noreferrer" target="_blank">{value}</a>
        }
        return `"${value}"`
    }

    render() {
        const type_name = 'string';
        const { props } = this;
        const { collapseStringsAfterLength, theme } = props;
        let { value } = props;
        let collapsible = toType(collapseStringsAfterLength) === 'integer';

        if (props.highlightSearch && `"${value}"`.toLowerCase().includes(props.highlightSearch.toLowerCase())) {
            return <div {...Theme(theme, 'string')}>
                <DataTypeLabel type_name={type_name} {...props} />
                {this.getHighlightValue(value)}
            </div>
        }

        if (collapsible && value.length > collapseStringsAfterLength) {
            this.style.cursor = 'pointer';
            if (this.state.collapsed) {
                value = (
                    <span>
                        {value.substring(0, collapseStringsAfterLength)}
                        <span {...Theme(theme, 'ellipsis')}> ...</span>
                    </span>
                );
            }
        }

        return (
            <div {...Theme(theme, 'string')}>
                <DataTypeLabel type_name={type_name} {...props} />
                <span
                    class="string-value"
                    style={this.style}
                    onClick={this.toggleCollapsed}
                >
                    {this.getValue(value)}
                </span>
            </div>
        );
    }
}
