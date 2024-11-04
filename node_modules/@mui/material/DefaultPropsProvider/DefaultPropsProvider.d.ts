import * as React from 'react';
import type { ComponentsPropsList } from '../styles/props';
declare function DefaultPropsProvider(props: React.PropsWithChildren<{
    value: {
        [P in keyof ComponentsPropsList]?: Partial<ComponentsPropsList[P]>;
    };
}>): React.JSX.Element;
declare namespace DefaultPropsProvider {
    var propTypes: any;
}
export default DefaultPropsProvider;
export declare function useDefaultProps<Props extends Record<string, any>>(params: {
    props: Props;
    name: string;
}): Props;
