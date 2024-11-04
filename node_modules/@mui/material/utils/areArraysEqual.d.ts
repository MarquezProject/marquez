type ItemComparer<Item> = (a: Item, b: Item) => boolean;
declare function areArraysEqual<Item>(array1: ReadonlyArray<Item>, array2: ReadonlyArray<Item>, itemComparer?: ItemComparer<Item>): boolean;
export default areArraysEqual;
