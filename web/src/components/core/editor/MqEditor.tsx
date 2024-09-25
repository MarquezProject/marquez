import React, {useEffect, useState} from "react";
import MonacoEditor from "react-monaco-editor/lib/editor";

interface Props {
  value: string;
  language: string;
}

export const MqEditor = ({value, language}: Props) => {
  const editorDidMount = (editor: any, monaco: any) => {
    editor.focus();
  }
  const onChange = (newValue: any, e: any) => {
    console.log('onChange', newValue, e);
  }

  const options = {
    selectOnLineNumbers: true,
    readOnly: true
  };

  return (
    <MonacoEditor
      width={'100%'}
      height={400}
      language={language}
      theme="vs-dark"
      value={value}
      options={options}
      editorDidMount={editorDidMount}
    />
  );
}