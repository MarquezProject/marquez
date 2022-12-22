"use strict"

//import react and reactDom for browser rendering
import React from "react"
import ReactDom from "react-dom"

//import the react-json-view component (installed with npm)
import JsonViewer from "./../../src/js/index"

const App = () => {
    const [search, setSearch] = React.useState('')

    return <div>
        Search: <input value={search} onChange={e => setSearch(e.target.value)} />
        <JsonViewer
            highlightSearch={search}
            style={{ padding: "10px"}}
            src={getExampleJson1()}
            displayObjectSize={true}
            name={"dev-server"}
            displayDataTypes={false}
            shouldCollapse={({ src, namespace, type }) => {
                if (type === "array" && src.indexOf("test") > -1) {
                    return true
                } else if (namespace.indexOf("moment") > -1) {
                    return true
                }
                return false
            }}
            defaultValue=""
            enableClipboard={false}
            theme={"rjv_white"}
        /></div>
}

//render 2 different examples of the react-json-view component
ReactDom.render(
    <App />,
    document.getElementById("app-container")
)

/*-------------------------------------------------------------------------*/
/*     the following functions just contain test json data for display     */
/*-------------------------------------------------------------------------*/

//just a function to get an example JSON object
function getExampleJson1() {
    return {
        string: "this is a test string",
        link: "https://mac-s-g.github.io/react-json-view/demo/dist/",
        integer: 42,
        empty_array: [],
        empty_object: {},
        array: [1, 2, 3, "test"],
        float: -2.757,
        undefined_var: undefined,
        parent: {
            sibling1: true,
            sibling2: false,
            sibling3: null,
        },
        string_number: "1234",
        moment: {
            a: 'shlomi',
            b: 'nir',
            c: {
                name: 'amir',
                d: [{
                    name: 'michael eran'
                }]
            }
        },
        date: new Date(),
        regexp: /[0-9]/gi
    }
}