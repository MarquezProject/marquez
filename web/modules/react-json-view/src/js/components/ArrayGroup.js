import React from 'react';
import Theme from './../themes/getStyle';

import VariableMeta from './VariableMeta';
import ObjectName from './ObjectName';
import ObjectComponent from './DataTypes/Object';

//icons
import { CollapsedIcon, ExpandedIcon } from './ToggleIcons';

//single indent is 5px
const SINGLE_INDENT = 5;

export default class extends React.PureComponent {
    constructor(props) {
        super(props);
        this.state = {
            expanded: {}
        };
    }

    componentDidMount() {
        this.updateExpandedGroups();
    }

    componentDidUpdate(prevProps) {
        if (prevProps.highlightSearch !== this.props.highlightSearch) {
            this.updateExpandedGroups();
        }
    }


    updateExpandedGroups = () => {
        const size = this.props.groupArraysAfterLength;
        const groups = Math.ceil(this.props.src.length / size);
        const expandedGroups = {};
        for (let i in [...Array(groups)]) {
            const groupSrc = this.props.src.slice(i * size, i * size + size)
            const groupContainingSearch = this.props.highlightSearch && JSON.stringify(groupSrc).toLowerCase().includes(this.props.highlightSearch.toLowerCase());
            if (groupContainingSearch) {
                expandedGroups[i] = true;
            }
        }

        this.setState({
            ...this.state,
            expanded: expandedGroups
        });
    }

    toggleCollapsed = (i) => {
        const groupState = !this.state.expanded[i];
        this.setState({
            ...this.state,
            expanded: {
                ...this.state.expanded,
                [i]: groupState,
            }
        });
    }

    getExpandedIcon(i) {
        const { theme, iconStyle } = this.props;

        if (this.state.expanded[i]) {
            return <ExpandedIcon {...{theme, iconStyle}}/>;
        }

        return <CollapsedIcon {...{theme, iconStyle}} />;
    }

    render() {
        const {
            src, groupArraysAfterLength, depth,
            name, theme, jsvRoot, namespace, parent_type,
            ...rest
        } = this.props;

        let expanded_icon, object_padding_left = 0;

        const array_group_padding_left = this.props.indentWidth * SINGLE_INDENT;

        if (!jsvRoot) {
            object_padding_left = this.props.indentWidth * SINGLE_INDENT;
        }

        const size = groupArraysAfterLength;
        const groups = Math.ceil(src.length / size);

        return (<div class='object-key-val'
            {...Theme(theme, jsvRoot ? 'jsv-root' : 'objectKeyVal', {paddingLeft: object_padding_left})}
        >
            <ObjectName {...this.props} />

            <span>
                <VariableMeta size={src.length} {...this.props}/>
            </span>
            {[...Array(groups)].map((_, i) =>
                <div key={i} class='object-key-val array-group' {...Theme(theme, 'objectKeyVal', {
                    marginLeft: 6,
                    paddingLeft: array_group_padding_left
                })} >
                    <span {...Theme(theme, 'brace-row')}>

                        <div class='icon-container' {...Theme(theme, 'icon-container')}
                            onClick={(e) => {this.toggleCollapsed(i);}}>
                            {this.getExpandedIcon(i)}
                        </div>
                        {this.state.expanded[i] ?
                            <ObjectComponent 
                                key={name + i}
                                depth={0}
                                name={false}
                                collapsed={false}
                                groupArraysAfterLength={size}
                                index_offset={i * size}
                                src={src.slice(i * size, i * size + size)}
                                namespace={namespace}
                                type="array"
                                parent_type="array_group"
                                theme={theme}
                                {...rest}
                            />
                            :   <span {...Theme(theme, 'brace')}  onClick={(e) => {this.toggleCollapsed(i);}} class='array-group-brace'>
                            [
                                <div {...Theme(theme, 'array-group-meta-data')} class='array-group-meta-data'>
                                    <span class='object-size'
                                        {...Theme(theme, 'object-size')}>
                                        {i * size}
                                        {' - '}
                                        {i * size + size > src.length ? src.length : i * size + size}
                                    </span>
                                </div>
                            ]
                            </span>
                        }

                    </span>
                </div>
            )}
        </div>
        );
    }

}
