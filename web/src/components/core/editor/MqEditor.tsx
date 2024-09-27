import React, {useEffect, useState} from "react";
import MonacoEditor from "react-monaco-editor/lib/editor";
import {Box} from "@mui/material";

interface Props {
  value: string;
  language: string;
  width: number | string;
  height: number | string;
}

export const MqEditor = ({value, language, width, height}: Props) => {
  const editorDidMount = (editor: any, monaco: any) => {
  }
  const onChange = (newValue: any, e: any) => {
    console.log('onChange', newValue, e);
  }

  const options = {
    selectOnLineNumbers: true,
    readOnly: true,
    glyphMargin: false,
    folding: false,
    lineNumbers: "off",
    lineDecorationsWidth: 0,
    lineNumbersMinChars: 0
  };

  return (
    <Box position={'relative'}>
    <MonacoEditor
      width={width}
      height={height}
      language={language}
      theme="vs-dark"
      value={value}
      onChange={onChange}
    />
    </Box>
  );
}