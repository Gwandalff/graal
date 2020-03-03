# Nodes and parsing of WASM rework

## Control Nodes

### Introduced

- WasmBr
- WasmBrIf
- WasmBrTable
- WasmReturn

### Modified

- WasmIfNode
- WasmLoopNode
- WasmBlocNode
- WasmIndirectCallNode
- WasmCallStubNode

## Operation Nodes

### Introduced

#### Local variables
- WasmLocalSet
- WasmLocalGet
- WasmLocalTee

#### Global Variable
- WasmGlobalSet
- WasmGlobalGet

#### Memory Management
- WasmLoad
- WasmStore
- WasmMemorySize
- WasmMemoryGrow

#### Constant
- WasmConst

#### Comparison
- WasmEQZ
- WasmEQ
- WasmNEQ
- WasmLT
- WasmGT
- WasmLE
- WasmGE

#### Bit counting for I32 and I64
- WasmCLZ
- WasmCTZ
- WasmPopCnt

#### Arithmetic for all types
- WasmAdd
- WasmSub
- WasmMul
- WasmDiv
- WasmRem

#### Binary Operation
- WasmAnd
- WasmOr
- WasmXor
- WasmShL
- WasmShR
- WasmRotL
- WasmRotR

#### Arithmetic for F32 and F64
- WasmAbs
- WasmNeg
- WasmCeil
- WasmFloor
- WasmTrunc
- WasmNearest
- WasmSqrt
- WasmMin
- WasmMax
- WasmCopySign

#### Type conversion
- WasmWrap
- WasmTruncConv
- WasmExtend
- WasmConvert
- WasmDemote
- WasmPromote
- WasmReinterpret
