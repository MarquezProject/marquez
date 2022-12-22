# searchable-react-json-view

This is a fork of [react-json-view](https://github.com/mac-s-g/react-json-view) with an extra search capability.  
Please go there for the full readme.

### Install
```sh
yarn add searchable-react-json-view
```

### New Props

Name|Type|Default|Description
|:---|:---|:---|:---
`highlightSearch`|`string`|None|What term to highlight - applies to number, string, boolean and object keys.
`highlightSearchColor`|`string`| <img src="https://user-images.githubusercontent.com/16322616/89119023-9018cb80-d4b3-11ea-8fca-8b068ce8ef71.png"/>|Highlight color
`highlightCurrentSearchColor`|`string`| <img src="https://user-images.githubusercontent.com/16322616/89119031-ac1c6d00-d4b3-11ea-9640-e5320904fdd3.png"/> | Current highlight color
`customCopiedIcon`|`JSX.Element`| null | Custom icon that appears after copying
`customCopyIcon`|`JSX.Element`| null | Custom icon for copy to clipboard
`customActions`|`array`| [] | Custom actions that appear after copy, edit etc. each item should be: { icon: JSX.Element, onClick: clickedJsonValue => void } 

### Example

<kbd><img src="https://user-images.githubusercontent.com/16322616/89118875-1d5b2080-d4b2-11ea-81fe-514d019cb26b.png" width="450" /></kbd>

### Custom actions & copy icon
```jsx
<JsonViewer
    customCopiedIcon={<span>Copied</span>}
    customCopyIcon={<span>Copy</span>}
    customActions={[{
        icon: <span>A</span>,
        onClick: (value) => alert(JSON.stringify(value))
    }]}
/>
```

