type SelectedValue = Node | Attr | string | number | boolean;
interface XPathSelect {
    (expression: string, node?: Node): Array<SelectedValue>;
    (expression: string, node: Node, single: true): SelectedValue;
}
export var select: XPathSelect;
export function select1(expression: string, node?: Node): SelectedValue;
export function evaluate(expression: string, contextNode: Node, resolver: XPathNSResolver, type: number, result: XPathResult): XPathResult;
export function useNamespaces(namespaceMap: { [name: string]: string }): XPathSelect;
