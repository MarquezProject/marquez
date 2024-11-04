import * as React from 'react';
declare function DefaultPropsProvider({ value, children, }: React.PropsWithChildren<{
    value: Record<string, any> | undefined;
}>): React.JSX.Element;
declare namespace DefaultPropsProvider {
    var propTypes: any;
}
export declare function useDefaultProps<Props>({ props, name }: {
    props: Props;
    name: string;
}): Props;
export default DefaultPropsProvider;
