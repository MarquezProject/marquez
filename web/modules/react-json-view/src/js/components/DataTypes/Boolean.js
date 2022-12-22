import React from 'react';
import DataTypeLabel from './DataTypeLabel';
import splitAndPushByDelimiter from './../../helpers/splitAndPushByDelimiter';

//theme
import Theme from './../../themes/getStyle';


export default class extends React.PureComponent {

    render() {
        const type_name = 'bool';
        const {props} = this;
        const {value, theme, highlightSearch, highlightSearchColor} = props;

        return <div {...Theme(theme, 'boolean')}>
            <DataTypeLabel type_name={type_name} {...props} />
            {splitAndPushByDelimiter(String(value), highlightSearch).map((char, i) => [
                <span
                    key={i}
                    style={{backgroundColor: i%2 === 1 ? highlightSearchColor : 'transparent'}}
                >
                    {char}
                </span>
            ])}
        </div>
    }

}
