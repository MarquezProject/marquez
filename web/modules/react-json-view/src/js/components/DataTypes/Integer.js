import React from 'react';
import DataTypeLabel from './DataTypeLabel';
import splitAndPushByDelimiter from './../../helpers/splitAndPushByDelimiter';

//theme
import Theme from './../../themes/getStyle';

export default class extends React.PureComponent {

    render() {
        const type_name = 'int';
        const {props} = this;
        const {value, theme, highlightSearch, highlightSearchColor} = props;

        return <div {...Theme(theme, 'integer')}>
            <DataTypeLabel type_name={type_name} {...props} />
            {splitAndPushByDelimiter(String(value), highlightSearch).map((digit, i) => [
                <span
                    key={i}
                    style={{backgroundColor: i%2 === 1 ? highlightSearchColor : 'transparent'}}
                >
                    {digit}
                </span>
            ])}
        </div>
    }

}
