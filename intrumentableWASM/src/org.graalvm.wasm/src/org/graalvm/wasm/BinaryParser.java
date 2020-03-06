/*
 * Copyright (c) 2019, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * The Universal Permissive License (UPL), Version 1.0
 *
 * Subject to the condition set forth below, permission is hereby granted to any
 * person obtaining a copy of this software, associated documentation and/or
 * data (collectively the "Software"), free of charge and under any and all
 * copyright rights in the Software, and any and all patent rights owned or
 * freely licensable by each licensor hereunder covering either (i) the
 * unmodified Software as contributed to or provided by such licensor, or (ii)
 * the Larger Works (as defined below), to deal in both
 *
 * (a) the Software, and
 *
 * (b) any piece of software and/or hardware listed in the lrgrwrks.txt file if
 * one is included with the Software each a "Larger Work" to which the Software
 * is contributed by such licensors),
 *
 * without restriction, including without limitation the rights to copy, create
 * derivative works of, display, perform, and distribute the Software and make,
 * use, sell, offer for sale, import, export, have made, and have sold the
 * Software and the Larger Work(s), and to sublicense the foregoing rights on
 * either these or other terms.
 *
 * This license is subject to the following condition:
 *
 * The above copyright notice and either this complete permission notice or at a
 * minimum a reference to the UPL must be included in all copies or substantial
 * portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package org.graalvm.wasm;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

import org.graalvm.wasm.TableRegistry.Table;
import org.graalvm.wasm.collection.ByteArrayList;
import org.graalvm.wasm.constants.CallIndirect;
import org.graalvm.wasm.constants.ExportIdentifier;
import org.graalvm.wasm.constants.GlobalModifier;
import org.graalvm.wasm.constants.ImportIdentifier;
import org.graalvm.wasm.constants.Instructions;
import org.graalvm.wasm.constants.LimitsPrefix;
import org.graalvm.wasm.constants.Section;
import org.graalvm.wasm.exception.WasmLinkerException;
import org.graalvm.wasm.memory.WasmMemory;
import org.graalvm.wasm.nodes.WasmEmptyNode;
import org.graalvm.wasm.nodes.WasmNode;
import org.graalvm.wasm.nodes.WasmRootNode;
import org.graalvm.wasm.nodes.bitsOp.WasmAnd;
import org.graalvm.wasm.nodes.bitsOp.WasmCLZ;
import org.graalvm.wasm.nodes.bitsOp.WasmOr;
import org.graalvm.wasm.nodes.bitsOp.WasmPopCnt;
import org.graalvm.wasm.nodes.bitsOp.WasmRotL;
import org.graalvm.wasm.nodes.bitsOp.WasmRotR;
import org.graalvm.wasm.nodes.bitsOp.WasmShL;
import org.graalvm.wasm.nodes.bitsOp.WasmShR;
import org.graalvm.wasm.nodes.bitsOp.WasmXor;
import org.graalvm.wasm.nodes.comparison.WasmEQ;
import org.graalvm.wasm.nodes.comparison.WasmEQZ;
import org.graalvm.wasm.nodes.comparison.WasmGE;
import org.graalvm.wasm.nodes.comparison.WasmGT;
import org.graalvm.wasm.nodes.comparison.WasmLE;
import org.graalvm.wasm.nodes.comparison.WasmLT;
import org.graalvm.wasm.nodes.comparison.WasmNEQ;
import org.graalvm.wasm.nodes.control.WasmBlockNode;
import org.graalvm.wasm.nodes.control.WasmBr;
import org.graalvm.wasm.nodes.control.WasmBrIf;
import org.graalvm.wasm.nodes.control.WasmBrTable;
import org.graalvm.wasm.nodes.control.WasmCallStubNode;
import org.graalvm.wasm.nodes.control.WasmDirectCallNode;
import org.graalvm.wasm.nodes.control.WasmIfNode;
import org.graalvm.wasm.nodes.control.WasmIndirectCallNode;
import org.graalvm.wasm.nodes.control.WasmIndirectCallWrapperNode;
import org.graalvm.wasm.nodes.control.WasmLoopNode;
import org.graalvm.wasm.nodes.control.WasmReturn;
import org.graalvm.wasm.nodes.conversion.WasmConvert;
import org.graalvm.wasm.nodes.conversion.WasmDemote;
import org.graalvm.wasm.nodes.conversion.WasmExtend;
import org.graalvm.wasm.nodes.conversion.WasmPromote;
import org.graalvm.wasm.nodes.conversion.WasmReinterpret;
import org.graalvm.wasm.nodes.conversion.WasmTruncConv;
import org.graalvm.wasm.nodes.conversion.WasmWrap;
import org.graalvm.wasm.nodes.memory.WasmLoad;
import org.graalvm.wasm.nodes.memory.WasmMemoryGrow;
import org.graalvm.wasm.nodes.memory.WasmMemorySize;
import org.graalvm.wasm.nodes.memory.WasmStore;
import org.graalvm.wasm.nodes.numeric.WasmAbs;
import org.graalvm.wasm.nodes.numeric.WasmAdd;
import org.graalvm.wasm.nodes.numeric.WasmCeil;
import org.graalvm.wasm.nodes.numeric.WasmCopySign;
import org.graalvm.wasm.nodes.numeric.WasmDiv;
import org.graalvm.wasm.nodes.numeric.WasmFloor;
import org.graalvm.wasm.nodes.numeric.WasmMax;
import org.graalvm.wasm.nodes.numeric.WasmMin;
import org.graalvm.wasm.nodes.numeric.WasmMul;
import org.graalvm.wasm.nodes.numeric.WasmNearest;
import org.graalvm.wasm.nodes.numeric.WasmNeg;
import org.graalvm.wasm.nodes.numeric.WasmRem;
import org.graalvm.wasm.nodes.numeric.WasmSqrt;
import org.graalvm.wasm.nodes.numeric.WasmSub;
import org.graalvm.wasm.nodes.numeric.WasmTrunc;
import org.graalvm.wasm.nodes.uncategorized.WasmDrop;
import org.graalvm.wasm.nodes.uncategorized.WasmNop;
import org.graalvm.wasm.nodes.uncategorized.WasmSelect;
import org.graalvm.wasm.nodes.uncategorized.WasmUnreachable;
import org.graalvm.wasm.nodes.variables.WasmConst;
import org.graalvm.wasm.nodes.variables.WasmGlobalGet;
import org.graalvm.wasm.nodes.variables.WasmGlobalSet;
import org.graalvm.wasm.nodes.variables.WasmLocalGet;
import org.graalvm.wasm.nodes.variables.WasmLocalSet;
import org.graalvm.wasm.nodes.variables.WasmLocalTee;

import com.oracle.truffle.api.RootCallTarget;
import com.oracle.truffle.api.Truffle;
import com.oracle.truffle.api.nodes.LoopNode;
import com.oracle.truffle.api.nodes.Node;
import com.oracle.truffle.api.nodes.RepeatingNode;

/**
 * Simple recursive-descend parser for the binary WebAssembly format.
 */
public class BinaryParser extends BinaryStreamParser {

    private static final int MAGIC = 0x6d736100;
    private static final int VERSION = 0x00000001;

    private WasmLanguage language;
    private WasmModule module;
    private byte[] bytesConsumed;
    private WasmRootNode[] rootNodes;

    /**
     * Modules may import, as well as define their own functions. Function IDs are shared among
     * imported and defined functions. This variable keeps track of the function indices, so that
     * imported and parsed code entries can be correctly associated to their respective functions
     * and types.
     */
    // TODO: We should remove this to reduce complexity - codeEntry state should be sufficient
    // to track the current largest function index.
    private int moduleFunctionIndex;

    BinaryParser(WasmLanguage language, WasmModule module, byte[] data) {
        super(data);
        this.language = language;
        this.module = module;
        this.bytesConsumed = new byte[1];
        this.moduleFunctionIndex = 0;
        this.rootNodes = null;
    }

    WasmModule readModule(WasmContext context) {
        validateMagicNumberAndVersion();
        readSections(context);
        return module;
    }

    private void validateMagicNumberAndVersion() {
        Assert.assertIntEqual(read4(), MAGIC, "Invalid MAGIC number");
        Assert.assertIntEqual(read4(), VERSION, "Invalid VERSION number");
    }

    private void readSections(WasmContext context) {
        while (!isEOF()) {
            byte sectionID = read1();
            int size = readUnsignedInt32();
            int startOffset = offset;
            switch (sectionID) {
                case Section.CUSTOM:
                    readCustomSection(size);
                    break;
                case Section.TYPE:
                    readTypeSection();
                    break;
                case Section.IMPORT:
                    readImportSection(context);
                    break;
                case Section.FUNCTION:
                    readFunctionSection();
                    break;
                case Section.TABLE:
                    readTableSection(context);
                    break;
                case Section.MEMORY:
                    readMemorySection(context);
                    break;
                case Section.GLOBAL:
                    readGlobalSection(context);
                    break;
                case Section.EXPORT:
                    readExportSection(context);
                    break;
                case Section.START:
                    readStartSection();
                    break;
                case Section.ELEMENT:
                    readElementSection(context);
                    break;
                case Section.CODE:
                    readCodeSection(context);
                    break;
                case Section.DATA:
                    readDataSection(context);
                    break;
                default:
                    Assert.fail("invalid section ID: " + sectionID);
            }
            Assert.assertIntEqual(offset - startOffset, size, String.format("Declared section (0x%02X) size is incorrect", sectionID));
        }
    }

    private void readCustomSection(int size) {
        // TODO: We skip the custom section for now, but we should see what we could typically pick
        // up here.
        offset += size;
    }

    private void readTypeSection() {
        int numTypes = readVectorLength();
        for (int t = 0; t != numTypes; ++t) {
            byte type = read1();
            switch (type) {
                case 0x60:
                    readFunctionType();
                    break;
                default:
                    Assert.fail("Only function types are supported in the type section");
            }
        }
    }

    private void readImportSection(WasmContext context) {
        Assert.assertIntEqual(module.symbolTable().maxGlobalIndex(), -1,
                        "The global index should be -1 when the import section is first read.");
        int numImports = readVectorLength();
        for (int i = 0; i != numImports; ++i) {
            String moduleName = readName();
            String memberName = readName();
            byte importType = readImportType();
            switch (importType) {
                case ImportIdentifier.FUNCTION: {
                    int typeIndex = readTypeIndex();
                    module.symbolTable().importFunction(context, moduleName, memberName, typeIndex);
                    moduleFunctionIndex++;
                    break;
                }
                case ImportIdentifier.TABLE: {
                    byte elemType = readElemType();
                    Assert.assertIntEqual(elemType, ReferenceTypes.FUNCREF, "Invalid element type for table import");
                    byte limitsPrefix = read1();
                    switch (limitsPrefix) {
                        case LimitsPrefix.NO_MAX: {
                            int initSize = readUnsignedInt32();  // initial size (in number of
                                                                 // entries)
                            module.symbolTable().importTable(context, moduleName, memberName, initSize, -1);
                            break;
                        }
                        case LimitsPrefix.WITH_MAX: {
                            int initSize = readUnsignedInt32();  // initial size (in number of
                                                                 // entries)
                            int maxSize = readUnsignedInt32();  // max size (in number of entries)
                            module.symbolTable().importTable(context, moduleName, memberName, initSize, maxSize);
                            break;
                        }
                        default:
                            Assert.fail(String.format("Invalid limits prefix for imported table (expected 0x00 or 0x01, got 0x%02X", limitsPrefix));
                    }
                    break;
                }
                case ImportIdentifier.MEMORY: {
                    byte limitsPrefix = read1();
                    switch (limitsPrefix) {
                        case LimitsPrefix.NO_MAX: {
                            // Read initial size (in number of entries).
                            int initSize = readUnsignedInt32();
                            int maxSize = -1;
                            module.symbolTable().importMemory(context, moduleName, memberName, initSize, maxSize);
                            break;
                        }
                        case LimitsPrefix.WITH_MAX: {
                            // Read initial size (in number of entries).
                            int initSize = readUnsignedInt32();
                            // Read max size (in number of entries).
                            int maxSize = readUnsignedInt32();
                            module.symbolTable().importMemory(context, moduleName, memberName, initSize, maxSize);
                            break;
                        }
                        default:
                            Assert.fail(String.format("Invalid limits prefix for imported memory (expected 0x00 or 0x01, got 0x%02X", limitsPrefix));
                    }
                    break;
                }
                case ImportIdentifier.GLOBAL: {
                    byte type = readValueType();
                    byte mutability = readMutability();
                    int index = module.symbolTable().maxGlobalIndex() + 1;
                    module.symbolTable().importGlobal(context, moduleName, memberName, index, type, mutability);
                    break;
                }
                default: {
                    Assert.fail(String.format("Invalid import type identifier: 0x%02X", importType));
                }
            }
        }
    }

    private void readFunctionSection() {
        int numFunctions = readVectorLength();
        for (int i = 0; i != numFunctions; ++i) {
            int functionTypeIndex = readUnsignedInt32();
            module.symbolTable().declareFunction(functionTypeIndex);
        }
    }

    private void readTableSection(WasmContext context) {
        int numTables = readVectorLength();
        Assert.assertIntLessOrEqual(module.symbolTable().tableCount() + numTables, 1, "Can import or declare at most one table per module.");
        // Since in the current version of WebAssembly supports at most one table instance per
        // module.
        // this loop should be executed at most once.
        for (byte tableIndex = 0; tableIndex != numTables; ++tableIndex) {
            byte elemType = readElemType();
            Assert.assertIntEqual(elemType, ReferenceTypes.FUNCREF, "Invalid element type for table");
            byte limitsPrefix = readLimitsPrefix();
            switch (limitsPrefix) {
                case LimitsPrefix.NO_MAX: {
                    int initSize = readUnsignedInt32();  // initial size (in number of entries)
                    module.symbolTable().allocateTable(context, initSize, -1);
                    break;
                }
                case LimitsPrefix.WITH_MAX: {
                    int initSize = readUnsignedInt32();  // initial size (in number of entries)
                    int maxSize = readUnsignedInt32();  // max size (in number of entries)
                    Assert.assertIntLessOrEqual(initSize, maxSize, "Initial table size must be smaller or equal than maximum size");
                    module.symbolTable().allocateTable(context, initSize, maxSize);
                    break;
                }
                default:
                    Assert.fail(String.format("Invalid limits prefix for table (expected 0x00 or 0x01, got 0x%02X", limitsPrefix));
            }
        }
    }

    private void readMemorySection(WasmContext context) {
        int numMemories = readVectorLength();
        Assert.assertIntLessOrEqual(module.symbolTable().memoryCount() + numMemories, 1, "Can import or declare at most one memory per module.");
        // Since in the current version of WebAssembly supports at most one table instance per
        // module.
        // this loop should be executed at most once.
        for (int i = 0; i != numMemories; ++i) {
            byte limitsPrefix = readLimitsPrefix();
            switch (limitsPrefix) {
                case LimitsPrefix.NO_MAX: {
                    // Read initial size (in Wasm pages).
                    int initSize = readUnsignedInt32();
                    int maxSize = -1;
                    module.symbolTable().allocateMemory(context, initSize, maxSize);
                    break;
                }
                case LimitsPrefix.WITH_MAX: {
                    // Read initial size (in Wasm pages).
                    int initSize = readUnsignedInt32();
                    // Read max size (in Wasm pages).
                    int maxSize = readUnsignedInt32();
                    module.symbolTable().allocateMemory(context, initSize, maxSize);
                    break;
                }
                default:
                    Assert.fail(String.format("Invalid limits prefix for memory (expected 0x00 or 0x01, got 0x%02X", limitsPrefix));
            }
        }
    }

    private void readCodeSection(WasmContext context) {
        int numCodeEntries = readVectorLength();
        rootNodes = new WasmRootNode[numCodeEntries];
        for (int entry = 0; entry != numCodeEntries; ++entry) {
            rootNodes[entry] = createCodeEntry(moduleFunctionIndex + entry);
        }
        for (int entryIndex = 0; entryIndex != numCodeEntries; ++entryIndex) {
            int codeEntrySize = readUnsignedInt32();
            int startOffset = offset;
            readCodeEntry(context, moduleFunctionIndex + entryIndex, rootNodes[entryIndex]);
            Assert.assertIntEqual(offset - startOffset, codeEntrySize, String.format("Code entry %d size is incorrect", entryIndex));
            context.linker().resolveCodeEntry(module, entryIndex);
        }
        moduleFunctionIndex += numCodeEntries;
    }

    private WasmRootNode createCodeEntry(int funcIndex) {
        final WasmFunction function = module.symbolTable().function(funcIndex);
        WasmCodeEntry codeEntry = new WasmCodeEntry(function, data);
        function.setCodeEntry(codeEntry);

        /*
         * Create the root node and create and set the call target for the body. This needs to be
         * done before reading the body block, because we need to be able to create direct call
         * nodes {@see TruffleRuntime#createDirectCallNode} during parsing.
         */
        WasmRootNode rootNode = new WasmRootNode(language, codeEntry);
        RootCallTarget callTarget = Truffle.getRuntime().createCallTarget(rootNode);
        function.setCallTarget(callTarget);

        return rootNode;
    }
    
    private void printBlock(WasmBlockNode block) {
    	WasmNode[] body = block.getEffectiveStatement();
        for (WasmNode wasmNode : body) {
			if (wasmNode instanceof WasmBlockNode) {
				System.out.println("start Nested Block");
				printBlock(((WasmBlockNode) wasmNode));
				System.out.println("end Nested Block");
			} else {
				System.out.println(wasmNode);
			}
		}
    }

    private void readCodeEntry(WasmContext context, int funcIndex, WasmRootNode rootNode) {
        /*
         * Initialise the code entry local variables (which contain the parameters and the locals).
         */
        initCodeEntryLocals(funcIndex);

        /* Read (parse) and abstractly interpret the code entry */
        final WasmFunction function = module.symbolTable().function(funcIndex);
        final byte returnTypeId = function.returnType();
        final int returnTypeLength = function.returnTypeLength();
        ExecutionState state = new ExecutionState();
        state.pushStackState(0);
        WasmBlockNode bodyBlock = readBlockBody(context, rootNode.codeEntry(), state, returnTypeId, returnTypeId, true);
        state.popStackState();
        Assert.assertIntEqual(state.stackSize(), returnTypeLength,
                        "Stack size must match the return type length at the function end");
        rootNode.setBody(bodyBlock);
        //printBlock(bodyBlock);

        /* Push a frame slot to the frame descriptor for every local. */
        rootNode.codeEntry().initLocalSlots(rootNode.getFrameDescriptor());

        /* Initialize the Truffle-related components required for execution. */
        rootNode.codeEntry().setByteConstants(state.byteConstants());
        rootNode.codeEntry().setIntConstants(state.intConstants());
        rootNode.codeEntry().setLongConstants(state.longConstants());
        rootNode.codeEntry().setBranchTables(state.branchTables());
        rootNode.codeEntry().initStackSlots(rootNode.getFrameDescriptor(), state.maxStackSize());
    }

    private ByteArrayList readCodeEntryLocals() {
        int numLocalsGroups = readVectorLength();
        ByteArrayList localTypes = new ByteArrayList();
        for (int localGroup = 0; localGroup < numLocalsGroups; localGroup++) {
            int groupLength = readVectorLength();
            byte t = readValueType();
            for (int i = 0; i != groupLength; ++i) {
                localTypes.add(t);
            }
        }
        return localTypes;
    }

    private void initCodeEntryLocals(int funcIndex) {
        WasmCodeEntry codeEntry = module.symbolTable().function(funcIndex).codeEntry();
        int typeIndex = module.symbolTable().function(funcIndex).typeIndex();
        ByteArrayList argumentTypes = module.symbolTable().functionTypeArgumentTypes(typeIndex);
        ByteArrayList localTypes = readCodeEntryLocals();
        byte[] allLocalTypes = ByteArrayList.concat(argumentTypes, localTypes);
        codeEntry.setLocalTypes(allLocalTypes);
    }

    @SuppressWarnings("unused")
    private static void checkValidStateOnBlockExit(byte returnTypeId, ExecutionState state, int initialStackSize) {
        if (returnTypeId == ValueTypes.VOID_TYPE) {
            Assert.assertIntEqual(state.stackSize(), initialStackSize, "Void function left values in the stack");
        } else {
            Assert.assertIntEqual(state.stackSize(), initialStackSize + 1, "Function left more than 1 values left in stack");
        }
    }

    private WasmBlockNode readBlock(WasmContext context, WasmCodeEntry codeEntry, ExecutionState state, boolean functionBlock) {
        byte blockTypeId = readBlockType();
        return readBlockBody(context, codeEntry, state, blockTypeId, blockTypeId, functionBlock);
    }

    private WasmLoopNode readLoop(WasmContext context, WasmCodeEntry codeEntry, ExecutionState state) {
        byte blockTypeId = readBlockType();
        return readLoop(context, codeEntry, state, blockTypeId);
    }

    private WasmBlockNode readBlockBody(WasmContext context, WasmCodeEntry codeEntry, ExecutionState state, byte returnTypeId, byte continuationTypeId, boolean functionBlock) {
        ArrayList<Node> nestedControlTable = new ArrayList<>();
        ArrayList<Node> callNodes = new ArrayList<>();
        int startStackSize = state.stackSize();
        int startOffset = offset();
        int startByteConstantOffset = state.byteConstantOffset();
        int startIntConstantOffset = state.intConstantOffset();
        int startLongConstantOffset = state.longConstantOffset();
        int startBranchTableOffset = state.branchTableOffset();
        WasmBlockNode currentBlock = new WasmBlockNode(module, codeEntry, startOffset, returnTypeId, continuationTypeId, startStackSize,
                        startByteConstantOffset, startIntConstantOffset, startLongConstantOffset, startBranchTableOffset, functionBlock);

        // Push the type length of the current block's continuation.
        // Used when branching out of nested blocks (br and br_if instructions).
        state.pushContinuationReturnLength(currentBlock.continuationTypeLength());
        
        int opcode;
        do {
            opcode = read1() & 0xFF;
            switch (opcode) {
                case Instructions.UNREACHABLE:
                	currentBlock.addStatement(new WasmUnreachable(module, codeEntry));
                    state.setReachable(false);
                    break;
                case Instructions.NOP:
                	currentBlock.addStatement(new WasmNop(module, codeEntry));
                    break;
                case Instructions.BLOCK: {
                    // Store the reachability of the current block, to restore it later.
                    boolean reachable = state.isReachable();
                    // Save the current block's stack pointer, in case we branch out of
                    // the nested block (continuation stack pointer).
                    int stackSize = state.stackSize();
                    state.pushStackState(stackSize);
                    WasmBlockNode nestedBlock = readBlock(context, codeEntry, state, false);
                    nestedControlTable.add(nestedBlock);
                    state.popStackState();
                    state.setReachable(reachable);
                    currentBlock.addStatement(nestedBlock);
                    break;
                }
                case Instructions.LOOP: {
                    // Store the reachability of the current block, to restore it later.
                    boolean reachable = state.isReachable();
                    // Save the current block's stack pointer, in case we branch out of
                    // the nested block (continuation stack pointer).
                    state.pushStackState(state.stackSize());
                    WasmLoopNode loopBlock = readLoop(context, codeEntry, state);
                    nestedControlTable.add(loopBlock);
                    state.popStackState();
                    state.setReachable(reachable);
                    currentBlock.addStatement(loopBlock);
                    break;
                }
                case Instructions.IF: {
                    // Pop the condition.
                    state.pop();
                    // Store the reachability of the current block, to restore it later.
                    boolean reachable = state.isReachable();
                    // Save the current block's stack pointer, in case we branch out of
                    // the nested block (continuation stack pointer).
                    // For the if block, we save the stack size reduced by 1, because of the
                    // condition value that will be popped before executing the if statement.
                    state.pushStackState(state.stackSize());
                    WasmIfNode ifNode = readIf(context, codeEntry, state);
                    nestedControlTable.add(ifNode);
                    state.popStackState();
                    state.setReachable(reachable);
                    currentBlock.addStatement(ifNode);
                    break;
                }
                case Instructions.ELSE:
                    // We handle the else instruction in the same way as the end instruction.
                case Instructions.END:
                    // If the end instruction is not reachable, then the stack size must be adjusted
                    // to match the stack size at the continuation point.
                    if (!state.isReachable()) {
                        state.setStackSize(state.getStackState(0) + state.getContinuationReturnLength(0));
                    }
                    // After the end instruction, the semantics of Wasm stack size require
                    // that we consider the code again reachable.
                    state.setReachable(true);
                    break;
                case Instructions.BR: {
                    // TODO: restore check
                    // This check was here to validate the stack size before branching and make sure
                    // that the block that is currently executing produced as many values as it
                    // was meant to before branching.
                    // We now have to postpone this check, as the target of a branch instruction may
                    // be more than one levels up, so the amount of values it should leave in
                    // the stack depends on the branch target.
                    // Assert.assertEquals(state.stackSize() - startStackSize,
                    // currentBlock.returnTypeLength(), "Invalid stack state on BR instruction");
                    int unwindLevel = readLabelIndex(bytesConsumed);
                    state.useLongConstant(unwindLevel);
                    state.useByteConstant(bytesConsumed[0]);
                    final int targetStackSize = state.getStackState(unwindLevel);
                    state.useIntConstant(targetStackSize);
                    final int continuationReturnLength = state.getContinuationReturnLength(unwindLevel);
                    state.useIntConstant(continuationReturnLength);
                    // This instruction is stack-polymorphic.
                    state.setReachable(false);
                    currentBlock.addStatement(new WasmBr(module, codeEntry, unwindLevel, targetStackSize, continuationReturnLength));
                    break;
                }
                case Instructions.BR_IF: {
                    state.pop();  // The branch condition.
                    // TODO: restore check
                    // This check was here to validate the stack size before branching and make sure
                    // that the block that is currently executing produced as many values as it
                    // was meant to before branching.
                    // We now have to postpone this check, as the target of a branch instruction may
                    // be more than one levels up, so the amount of values it should leave in the
                    // stack depends on the branch target.
                    // Assert.assertEquals(state.stackSize() - startStackSize,
                    // currentBlock.returnTypeLength(), "Invalid stack state on BR instruction");
                    int unwindLevel = readLabelIndex(bytesConsumed);
                    state.useLongConstant(unwindLevel);
                    state.useByteConstant(bytesConsumed[0]);
                    state.useIntConstant(state.getStackState(unwindLevel));
                    state.useIntConstant(state.getContinuationReturnLength(unwindLevel));
                    currentBlock.addStatement(new WasmBrIf(module, codeEntry, unwindLevel, state.getStackState(unwindLevel), state.getContinuationReturnLength(unwindLevel)));
                    break;
                }
                case Instructions.BR_TABLE: {
                    state.pop();
                    int numLabels = readVectorLength();
                    // We need to save three tables here, to maintain the mapping target -> state
                    // mapping:
                    // - the length of the return type
                    // - a table containing the branch targets for the instruction
                    // - a table containing the stack state for each corresponding branch target
                    // We encode this in a single array.
                    int[] branchTable = new int[2 * (numLabels + 1) + 1];
                    int returnLength = -1;
                    // The BR_TABLE instruction behaves like a 'switch' statement.
                    // There is one extra label for the 'default' case.
                    for (int i = 0; i != numLabels + 1; ++i) {
                        final int unwindLevel = readLabelIndex();
                        branchTable[1 + 2 * i + 0] = unwindLevel;
                        branchTable[1 + 2 * i + 1] = state.getStackState(unwindLevel);
                        final int blockReturnLength = state.getContinuationReturnLength(unwindLevel);
                        if (returnLength == -1) {
                            returnLength = blockReturnLength;
                        } else {
                            Assert.assertIntEqual(returnLength, blockReturnLength,
                                            "All target blocks in br.table must have the same return type length.");
                        }
                    }
                    branchTable[0] = returnLength;
                    // The offset to the branch table.
                    int offset = state.branchTableOffset();
                    state.saveBranchTable(branchTable);
                    // This instruction is stack-polymorphic.
                    state.setReachable(false);
                    currentBlock.addStatement(new WasmBrTable(module, codeEntry, offset) );
                    break;
                }
                case Instructions.RETURN: {
                    // Pop the stack values used as the return values.
                    for (int i = 0; i < codeEntry.function().returnTypeLength(); i++) {
                        state.pop();
                    }
                    state.useLongConstant(state.stackStateCount());
                    state.useIntConstant(state.getRootBlockReturnLength());
                    // This instruction is stack-polymorphic.
                    state.setReachable(false);
                    currentBlock.addStatement(new WasmReturn(module, codeEntry) );
                    break;
                }
                case Instructions.CALL: {
                    int functionIndex = readFunctionIndex(bytesConsumed);
                    state.useLongConstant(functionIndex);
                    state.useByteConstant(bytesConsumed[0]);
                    WasmFunction function = module.symbolTable().function(functionIndex);
                    state.pop(function.numArguments());
                    state.push(function.returnTypeLength());

                    // We deliberately do not create the call node during parsing,
                    // because the call target is only created after the code entry is parsed.
                    // The code entry might not be yet parsed when we encounter this call.
                    //
                    // Furthermore, if the call target is imported from another module,
                    // then that other module might not have been parsed yet.
                    // Therefore, the call node will be created lazily during linking,
                    // after the call target from the other module exists.
                    WasmCallStubNode stub = new WasmCallStubNode(function);
                    callNodes.add(stub);
                    WasmDirectCallNode call = new WasmDirectCallNode(module, codeEntry, functionIndex, stub);
                    call.setCallTarget(Truffle.getRuntime().createDirectCallNode(function.resolveCallTarget()));
                    context.linker().resolveCallsite(module, call, callNodes.size() - 1, function);
                    
                    currentBlock.addStatement(call);
                    break;
                }
                case Instructions.CALL_INDIRECT: {
                    int expectedFunctionTypeIndex = readTypeIndex(bytesConsumed);
                    state.useLongConstant(expectedFunctionTypeIndex);
                    state.useByteConstant(bytesConsumed[0]);
                    int numArguments = module.symbolTable().functionTypeArgumentCount(expectedFunctionTypeIndex);
                    int returnLength = module.symbolTable().functionTypeReturnTypeLength(expectedFunctionTypeIndex);

                    // Pop the function index to call, then pop the arguments and push the return
                    // value.
                    state.pop();
                    state.pop(numArguments);
                    state.push(returnLength);
                    WasmIndirectCallNode indirectCall = WasmIndirectCallNode.create();
                    callNodes.add(indirectCall);
                    Assert.assertIntEqual(read1(), CallIndirect.ZERO_TABLE, "CALL_INDIRECT: Instruction must end with 0x00");
                    currentBlock.addStatement(new WasmIndirectCallWrapperNode(module, codeEntry, expectedFunctionTypeIndex, indirectCall));
                    break;
                }
                case Instructions.DROP:
                    state.pop();
                    currentBlock.addStatement(new WasmDrop(module, codeEntry) );
                    break;
                case Instructions.SELECT:
                    // Pop three values from the stack: the condition and the values to select
                    // between.
                    state.pop(3);
                    state.push();
                    currentBlock.addStatement(new WasmSelect(module, codeEntry) );
                    break;
                case Instructions.LOCAL_GET: {
                    int localIndex = readLocalIndex(bytesConsumed);
                    state.useLongConstant(localIndex);
                    state.useByteConstant(bytesConsumed[0]);
                    // Assert localIndex exists.
                    Assert.assertIntLessOrEqual(localIndex, codeEntry.numLocals(), "Invalid local index for local.get");
                    state.push();
                    currentBlock.addStatement(new WasmLocalGet(module, codeEntry, localIndex) );
                    break;
                }
                case Instructions.LOCAL_SET: {
                    int localIndex = readLocalIndex(bytesConsumed);
                    state.useLongConstant(localIndex);
                    state.useByteConstant(bytesConsumed[0]);
                    // Assert localIndex exists.
                    Assert.assertIntLessOrEqual(localIndex, codeEntry.numLocals(), "Invalid local index for local.set");
                    // Assert there is a value on the top of the stack.
                    Assert.assertIntGreater(state.stackSize(), 0, "local.set requires at least one element in the stack");
                    state.pop();
                    currentBlock.addStatement(new WasmLocalSet(module, codeEntry, localIndex) );
                    break;
                }
                case Instructions.LOCAL_TEE: {
                    int localIndex = readLocalIndex(bytesConsumed);
                    state.useLongConstant(localIndex);
                    state.useByteConstant(bytesConsumed[0]);
                    // Assert localIndex exists.
                    Assert.assertIntLessOrEqual(localIndex, codeEntry.numLocals(), "Invalid local index for local.tee");
                    // Assert there is a value on the top of the stack.
                    Assert.assertIntGreater(state.stackSize(), 0, "local.tee requires at least one element in the stack");
                    currentBlock.addStatement(new WasmLocalTee(module, codeEntry, localIndex) );
                    break;
                }
                case Instructions.GLOBAL_GET: {
                    int index = readLocalIndex(bytesConsumed);
                    state.useLongConstant(index);
                    state.useByteConstant(bytesConsumed[0]);
                    Assert.assertIntLessOrEqual(index, module.symbolTable().maxGlobalIndex(),
                                    "Invalid global index for global.get.");
                    state.push();
                    currentBlock.addStatement(new WasmGlobalGet(module, codeEntry, index) );
                    break;
                }
                case Instructions.GLOBAL_SET: {
                    int index = readLocalIndex(bytesConsumed);
                    state.useLongConstant(index);
                    state.useByteConstant(bytesConsumed[0]);
                    // Assert localIndex exists.
                    Assert.assertIntLessOrEqual(index, module.symbolTable().maxGlobalIndex(),
                                    "Invalid global index for global.set.");
                    // Assert that the global is mutable.
                    Assert.assertTrue(module.symbolTable().globalMutability(index) == GlobalModifier.MUTABLE,
                                    "Immutable globals cannot be set: " + index);
                    // Assert there is a value on the top of the stack.
                    Assert.assertIntGreater(state.stackSize(), 0, "global.set requires at least one element in the stack");
                    state.pop();
                    currentBlock.addStatement(new WasmGlobalSet(module, codeEntry, index) );
                    break;
                }
                case Instructions.I32_LOAD:
                case Instructions.I64_LOAD:
                case Instructions.F32_LOAD:
                case Instructions.F64_LOAD:
                case Instructions.I32_LOAD8_S:
                case Instructions.I32_LOAD8_U:
                case Instructions.I32_LOAD16_S:
                case Instructions.I32_LOAD16_U:
                case Instructions.I64_LOAD8_S:
                case Instructions.I64_LOAD8_U:
                case Instructions.I64_LOAD16_S:
                case Instructions.I64_LOAD16_U:
                case Instructions.I64_LOAD32_S:
                case Instructions.I64_LOAD32_U: {
                    // We don't store the `align` literal, as our implementation does not make use
                    // of it, but we need to store it's byte length, so that we can skip it
                    // during execution.
                    readUnsignedInt32(bytesConsumed);
                    // Set consume count for the bytes.
                    state.useByteConstant(bytesConsumed[0]);
                    int loadOffset = readUnsignedInt32(bytesConsumed);
                    state.useLongConstant(loadOffset);
                    state.useByteConstant(bytesConsumed[0]);
                    Assert.assertIntGreater(state.stackSize(), 0, String.format("load instruction 0x%02X requires at least one element in the stack", opcode));
                    state.pop();   // Base address.
                    state.push();  // Loaded value.
                    currentBlock.addStatement(new WasmLoad(module, codeEntry, opcode, loadOffset) );
                    break;
                }
                case Instructions.I32_STORE:
                case Instructions.I64_STORE:
                case Instructions.F32_STORE:
                case Instructions.F64_STORE:
                case Instructions.I32_STORE_8:
                case Instructions.I32_STORE_16:
                case Instructions.I64_STORE_8:
                case Instructions.I64_STORE_16:
                case Instructions.I64_STORE_32: {
                    readUnsignedInt32(bytesConsumed);  // align
                    // We don't store the `align` literal, as our implementation does not make use
                    // of it,but we need to store it's byte length, so that we can skip it
                    // during the execution.
                    state.useByteConstant(bytesConsumed[0]);
                    int storeOffset = readUnsignedInt32(bytesConsumed);
                    state.useLongConstant(storeOffset);
                    state.useByteConstant(bytesConsumed[0]);
                    Assert.assertIntGreater(state.stackSize(), 1, String.format("store instruction 0x%02X requires at least two elements in the stack", opcode));
                    state.pop();  // Value to store.
                    state.pop();  // Base address.
                    currentBlock.addStatement(new WasmStore(module, codeEntry, opcode, storeOffset) );
                    break;
                }
                case Instructions.MEMORY_SIZE: {
                    // Skip the constant 0x00.
                    read1();
                    state.push();
                    currentBlock.addStatement(new WasmMemorySize(module, codeEntry));
                    break;
                }
                case Instructions.MEMORY_GROW: {
                    // Skip the constant 0x00.
                    read1();
                    state.pop();
                    state.push();
                    currentBlock.addStatement(new WasmMemoryGrow(module, codeEntry));
                    break;
                }
                case Instructions.I32_CONST: {
                    int value = readSignedInt32(bytesConsumed);
                    state.useLongConstant(value);
                    state.useByteConstant(bytesConsumed[0]);
                    state.push();
                    currentBlock.addStatement(new WasmConst(module, codeEntry, ValueTypes.I32_TYPE, value));
                    break;
                }
                case Instructions.I64_CONST: {
                    long value = readSignedInt64(bytesConsumed);
                    state.useLongConstant(value);
                    state.useByteConstant(bytesConsumed[0]);
                    state.push();
                    currentBlock.addStatement(new WasmConst(module, codeEntry, ValueTypes.I64_TYPE, value));
                    break;
                }
                case Instructions.F32_CONST: {
                    int value = readFloatAsInt32();
                    state.useLongConstant(value);
                    state.push();
                    currentBlock.addStatement(new WasmConst(module, codeEntry, ValueTypes.F32_TYPE, value));
                    break;
                }
                case Instructions.F64_CONST: {
                    long value = readFloatAsInt64();
                    state.useLongConstant(value);
                    state.push();
                    currentBlock.addStatement(new WasmConst(module, codeEntry, ValueTypes.F64_TYPE, value));
                    break;
                }
                case Instructions.I32_EQZ:
                    state.pop();
                    state.push();
                    currentBlock.addStatement(new WasmEQZ(module, codeEntry, ValueTypes.I32_TYPE));
                    break;
                case Instructions.I32_EQ:
                	state.pop();
                    state.pop();
                    state.push();
                    currentBlock.addStatement(new WasmEQ(module, codeEntry, ValueTypes.I32_TYPE));
                    break;
                case Instructions.I32_NE:
                	state.pop();
                    state.pop();
                    state.push();
                    currentBlock.addStatement(new WasmNEQ(module, codeEntry, ValueTypes.I32_TYPE));
                    break;
                case Instructions.I32_LT_S:
                	state.pop();
                    state.pop();
                    state.push();
                    currentBlock.addStatement(new WasmLT(module, codeEntry, ValueTypes.I32_TYPE, true));
                    break;
                case Instructions.I32_LT_U:
                	state.pop();
                    state.pop();
                    state.push();
                    currentBlock.addStatement(new WasmLT(module, codeEntry, ValueTypes.I32_TYPE, false));
                    break;
                case Instructions.I32_GT_S:
                	state.pop();
                    state.pop();
                    state.push();
                    currentBlock.addStatement(new WasmGT(module, codeEntry, ValueTypes.I32_TYPE, true));
                    break;
                case Instructions.I32_GT_U:
                	state.pop();
                    state.pop();
                    state.push();
                    currentBlock.addStatement(new WasmGT(module, codeEntry, ValueTypes.I32_TYPE, false));
                    break;
                case Instructions.I32_LE_S:
                	state.pop();
                    state.pop();
                    state.push();
                    currentBlock.addStatement(new WasmLE(module, codeEntry, ValueTypes.I32_TYPE, true));
                    break;
                case Instructions.I32_LE_U:
                	state.pop();
                    state.pop();
                    state.push();
                    currentBlock.addStatement(new WasmLE(module, codeEntry, ValueTypes.I32_TYPE, false));
                    break;
                case Instructions.I32_GE_S:
                	state.pop();
                    state.pop();
                    state.push();
                    currentBlock.addStatement(new WasmGE(module, codeEntry, ValueTypes.I32_TYPE, true));
                    break;
                case Instructions.I32_GE_U:
                    state.pop();
                    state.pop();
                    state.push();
                    currentBlock.addStatement(new WasmGE(module, codeEntry, ValueTypes.I32_TYPE, false));
                    break;
                case Instructions.I64_EQZ:
                    state.pop();
                    state.push();
                    currentBlock.addStatement(new WasmEQZ(module, codeEntry, ValueTypes.I64_TYPE));
                    break;
                case Instructions.I64_EQ:
                	state.pop();
                    state.pop();
                    state.push();
                    currentBlock.addStatement(new WasmEQ(module, codeEntry, ValueTypes.I64_TYPE));
                    break;
                case Instructions.I64_NE:
                	state.pop();
                    state.pop();
                    state.push();
                    currentBlock.addStatement(new WasmNEQ(module, codeEntry, ValueTypes.I64_TYPE));
                    break;
                case Instructions.I64_LT_S:
                	state.pop();
                    state.pop();
                    state.push();
                    currentBlock.addStatement(new WasmLT(module, codeEntry, ValueTypes.I64_TYPE, true));
                    break;
                case Instructions.I64_LT_U:
                	state.pop();
                    state.pop();
                    state.push();
                    currentBlock.addStatement(new WasmLT(module, codeEntry, ValueTypes.I64_TYPE, false));
                    break;
                case Instructions.I64_GT_S:
                	state.pop();
                    state.pop();
                    state.push();
                    currentBlock.addStatement(new WasmGT(module, codeEntry, ValueTypes.I64_TYPE, true));
                    break;
                case Instructions.I64_GT_U:
                	state.pop();
                    state.pop();
                    state.push();
                    currentBlock.addStatement(new WasmGT(module, codeEntry, ValueTypes.I64_TYPE, false));
                    break;
                case Instructions.I64_LE_S:
                	state.pop();
                    state.pop();
                    state.push();
                    currentBlock.addStatement(new WasmLE(module, codeEntry, ValueTypes.I64_TYPE, true));
                    break;
                case Instructions.I64_LE_U:
                	state.pop();
                    state.pop();
                    state.push();
                    currentBlock.addStatement(new WasmLE(module, codeEntry, ValueTypes.I64_TYPE, false));
                    break;
                case Instructions.I64_GE_S:
                	state.pop();
                    state.pop();
                    state.push();
                    currentBlock.addStatement(new WasmGE(module, codeEntry, ValueTypes.I64_TYPE, true));
                    break;
                case Instructions.I64_GE_U:
                    state.pop();
                    state.pop();
                    state.push();
                    currentBlock.addStatement(new WasmGE(module, codeEntry, ValueTypes.I64_TYPE, false));
                    break;
                case Instructions.F32_EQ:
                	state.pop();
                    state.pop();
                    state.push();
                    currentBlock.addStatement(new WasmEQ(module, codeEntry, ValueTypes.F32_TYPE));
                    break;
                case Instructions.F32_NE:
                	state.pop();
                    state.pop();
                    state.push();
                    currentBlock.addStatement(new WasmNEQ(module, codeEntry, ValueTypes.F32_TYPE));
                    break;
                case Instructions.F32_LT:
                	state.pop();
                    state.pop();
                    state.push();
                    currentBlock.addStatement(new WasmLT(module, codeEntry, ValueTypes.F32_TYPE));
                    break;
                case Instructions.F32_GT:
                	state.pop();
                    state.pop();
                    state.push();
                    currentBlock.addStatement(new WasmGT(module, codeEntry, ValueTypes.F32_TYPE));
                    break;
                case Instructions.F32_LE:
                	state.pop();
                    state.pop();
                    state.push();
                    currentBlock.addStatement(new WasmLE(module, codeEntry, ValueTypes.F32_TYPE));
                    break;
                case Instructions.F32_GE:
                    state.pop();
                    state.pop();
                    state.push();
                    currentBlock.addStatement(new WasmGE(module, codeEntry, ValueTypes.F32_TYPE));
                    break;
                case Instructions.F64_EQ:
                	state.pop();
                    state.pop();
                    state.push();
                    currentBlock.addStatement(new WasmEQ(module, codeEntry, ValueTypes.F64_TYPE));
                    break;
                case Instructions.F64_NE:
                	state.pop();
                    state.pop();
                    state.push();
                    currentBlock.addStatement(new WasmNEQ(module, codeEntry, ValueTypes.F64_TYPE));
                    break;
                case Instructions.F64_LT:
                	state.pop();
                    state.pop();
                    state.push();
                    currentBlock.addStatement(new WasmLT(module, codeEntry, ValueTypes.F64_TYPE));
                    break;
                case Instructions.F64_GT:
                	state.pop();
                    state.pop();
                    state.push();
                    currentBlock.addStatement(new WasmGT(module, codeEntry, ValueTypes.F64_TYPE));
                    break;
                case Instructions.F64_LE:
                	state.pop();
                    state.pop();
                    state.push();
                    currentBlock.addStatement(new WasmLE(module, codeEntry, ValueTypes.F64_TYPE));
                    break;
                case Instructions.F64_GE:
                    state.pop();
                    state.pop();
                    state.push();
                    currentBlock.addStatement(new WasmGE(module, codeEntry, ValueTypes.F64_TYPE));
                    break;
                case Instructions.I32_CLZ:
                	state.pop();
                    state.push();
                    currentBlock.addStatement(new WasmCLZ(module, codeEntry, ValueTypes.I32_TYPE));
                    break;
                case Instructions.I32_CTZ:
                	state.pop();
                    state.push();
                    currentBlock.addStatement(new WasmCLZ(module, codeEntry, ValueTypes.I32_TYPE));
                    break;
                case Instructions.I32_POPCNT:
                    state.pop();
                    state.push();
                    currentBlock.addStatement(new WasmPopCnt(module, codeEntry, ValueTypes.I32_TYPE));
                    break;
                case Instructions.I32_ADD:
                	state.pop();
                    state.pop();
                    state.push();
                    currentBlock.addStatement(new WasmAdd(module, codeEntry, ValueTypes.I32_TYPE));
                    break;
                case Instructions.I32_SUB:
                	state.pop();
                    state.pop();
                    state.push();
                    currentBlock.addStatement(new WasmSub(module, codeEntry, ValueTypes.I32_TYPE));
                    break;
                case Instructions.I32_MUL:
                	state.pop();
                    state.pop();
                    state.push();
                    currentBlock.addStatement(new WasmMul(module, codeEntry, ValueTypes.I32_TYPE));
                    break;
                case Instructions.I32_DIV_S:
                case Instructions.I32_DIV_U:
                	state.pop();
                    state.pop();
                    state.push();
                    currentBlock.addStatement(new WasmDiv(module, codeEntry, ValueTypes.I32_TYPE, opcode == Instructions.I32_DIV_S));
                    break;
                case Instructions.I32_REM_S:
                case Instructions.I32_REM_U:
                	state.pop();
                    state.pop();
                    state.push();
                    currentBlock.addStatement(new WasmRem(module, codeEntry, ValueTypes.I32_TYPE, opcode == Instructions.I32_DIV_S));
                    break;
                case Instructions.I32_AND:
                	state.pop();
                    state.pop();
                    state.push();
                    currentBlock.addStatement(new WasmAnd(module, codeEntry, ValueTypes.I32_TYPE));
                    break;
                case Instructions.I32_OR:
                	state.pop();
                    state.pop();
                    state.push();
                    currentBlock.addStatement(new WasmOr(module, codeEntry, ValueTypes.I32_TYPE));
                    break;
                case Instructions.I32_XOR:
                	state.pop();
                    state.pop();
                    state.push();
                    currentBlock.addStatement(new WasmXor(module, codeEntry, ValueTypes.I32_TYPE));
                    break;
                case Instructions.I32_SHL:
                	state.pop();
                    state.pop();
                    state.push();
                    currentBlock.addStatement(new WasmShL(module, codeEntry, ValueTypes.I32_TYPE));
                    break;
                case Instructions.I32_SHR_S:
                case Instructions.I32_SHR_U:
                	state.pop();
                    state.pop();
                    state.push();
                    currentBlock.addStatement(new WasmShR(module, codeEntry, ValueTypes.I32_TYPE, opcode == Instructions.I32_DIV_S));
                    break;
                case Instructions.I32_ROTL:
                	state.pop();
                    state.pop();
                    state.push();
                    currentBlock.addStatement(new WasmRotL(module, codeEntry, ValueTypes.I32_TYPE));
                    break;
                case Instructions.I32_ROTR:
                    state.pop();
                    state.pop();
                    state.push();
                    currentBlock.addStatement(new WasmRotR(module, codeEntry, ValueTypes.I32_TYPE));
                    break;
                case Instructions.I64_CLZ:
                	state.pop();
                    state.push();
                    currentBlock.addStatement(new WasmCLZ(module, codeEntry, ValueTypes.I64_TYPE));
                    break;
                case Instructions.I64_CTZ:
                	state.pop();
                    state.push();
                    currentBlock.addStatement(new WasmCLZ(module, codeEntry, ValueTypes.I64_TYPE));
                    break;
                case Instructions.I64_POPCNT:
                    state.pop();
                    state.push();
                    currentBlock.addStatement(new WasmPopCnt(module, codeEntry, ValueTypes.I64_TYPE));
                    break;
                case Instructions.I64_ADD:
                	state.pop();
                    state.pop();
                    state.push();
                    currentBlock.addStatement(new WasmAdd(module, codeEntry, ValueTypes.I64_TYPE));
                    break;
                case Instructions.I64_SUB:
                	state.pop();
                    state.pop();
                    state.push();
                    currentBlock.addStatement(new WasmSub(module, codeEntry, ValueTypes.I64_TYPE));
                    break;
                case Instructions.I64_MUL:
                	state.pop();
                    state.pop();
                    state.push();
                    currentBlock.addStatement(new WasmMul(module, codeEntry, ValueTypes.I64_TYPE));
                    break;
                case Instructions.I64_DIV_S:
                case Instructions.I64_DIV_U:
                	state.pop();
                    state.pop();
                    state.push();
                    currentBlock.addStatement(new WasmDiv(module, codeEntry, ValueTypes.I64_TYPE, opcode == Instructions.I64_DIV_S));
                    break;
                case Instructions.I64_REM_S:
                case Instructions.I64_REM_U:
                	state.pop();
                    state.pop();
                    state.push();
                    currentBlock.addStatement(new WasmRem(module, codeEntry, ValueTypes.I64_TYPE, opcode == Instructions.I64_DIV_S));
                    break;
                case Instructions.I64_AND:
                	state.pop();
                    state.pop();
                    state.push();
                    currentBlock.addStatement(new WasmAnd(module, codeEntry, ValueTypes.I64_TYPE));
                    break;
                case Instructions.I64_OR:
                	state.pop();
                    state.pop();
                    state.push();
                    currentBlock.addStatement(new WasmOr(module, codeEntry, ValueTypes.I64_TYPE));
                    break;
                case Instructions.I64_XOR:
                	state.pop();
                    state.pop();
                    state.push();
                    currentBlock.addStatement(new WasmXor(module, codeEntry, ValueTypes.I64_TYPE));
                    break;
                case Instructions.I64_SHL:
                	state.pop();
                    state.pop();
                    state.push();
                    currentBlock.addStatement(new WasmShL(module, codeEntry, ValueTypes.I64_TYPE));
                    break;
                case Instructions.I64_SHR_S:
                case Instructions.I64_SHR_U:
                	state.pop();
                    state.pop();
                    state.push();
                    currentBlock.addStatement(new WasmShR(module, codeEntry, ValueTypes.I64_TYPE, opcode == Instructions.I64_DIV_S));
                    break;
                case Instructions.I64_ROTL:
                	state.pop();
                    state.pop();
                    state.push();
                    currentBlock.addStatement(new WasmRotL(module, codeEntry, ValueTypes.I64_TYPE));
                    break;
                case Instructions.I64_ROTR:
                    state.pop();
                    state.pop();
                    state.push();
                    currentBlock.addStatement(new WasmRotR(module, codeEntry, ValueTypes.I64_TYPE));
                    break;
                case Instructions.F32_ABS:
                	state.pop();
                    state.push();
                    currentBlock.addStatement(new WasmAbs(module, codeEntry, ValueTypes.F32_TYPE));
                    break;
                case Instructions.F32_NEG:
                	state.pop();
                    state.push();
                    currentBlock.addStatement(new WasmNeg(module, codeEntry, ValueTypes.F32_TYPE));
                    break;
                case Instructions.F32_CEIL:
                	state.pop();
                    state.push();
                    currentBlock.addStatement(new WasmCeil(module, codeEntry, ValueTypes.F32_TYPE));
                    break;
                case Instructions.F32_FLOOR:
                	state.pop();
                    state.push();
                    currentBlock.addStatement(new WasmFloor(module, codeEntry, ValueTypes.F32_TYPE));
                    break;
                case Instructions.F32_TRUNC:
                	state.pop();
                    state.push();
                    currentBlock.addStatement(new WasmTrunc(module, codeEntry, ValueTypes.F32_TYPE));
                    break;
                case Instructions.F32_NEAREST:
                	state.pop();
                    state.push();
                    currentBlock.addStatement(new WasmNearest(module, codeEntry, ValueTypes.F32_TYPE));
                    break;
                case Instructions.F32_SQRT:
                    state.pop();
                    state.push();
                    currentBlock.addStatement(new WasmSqrt(module, codeEntry, ValueTypes.F32_TYPE));
                    break;
                case Instructions.F32_ADD:
                	state.pop();
                    state.pop();
                    state.push();
                    currentBlock.addStatement(new WasmAdd(module, codeEntry, ValueTypes.F32_TYPE));
                    break;
                case Instructions.F32_SUB:
                	state.pop();
                    state.pop();
                    state.push();
                    currentBlock.addStatement(new WasmSub(module, codeEntry, ValueTypes.F32_TYPE));
                    break;
                case Instructions.F32_MUL:
                	state.pop();
                    state.pop();
                    state.push();
                    currentBlock.addStatement(new WasmMul(module, codeEntry, ValueTypes.F32_TYPE));
                    break;
                case Instructions.F32_DIV:
                	state.pop();
                    state.pop();
                    state.push();
                    currentBlock.addStatement(new WasmDiv(module, codeEntry, ValueTypes.F32_TYPE));
                    break;
                case Instructions.F32_MIN:
                	state.pop();
                    state.pop();
                    state.push();
                    currentBlock.addStatement(new WasmMin(module, codeEntry, ValueTypes.F32_TYPE));
                    break;
                case Instructions.F32_MAX:
                	state.pop();
                    state.pop();
                    state.push();
                    currentBlock.addStatement(new WasmMax(module, codeEntry, ValueTypes.F32_TYPE));
                    break;
                case Instructions.F32_COPYSIGN:
                    state.pop();
                    state.pop();
                    state.push();
                    currentBlock.addStatement(new WasmCopySign(module, codeEntry, ValueTypes.F32_TYPE));
                    break;
                case Instructions.F64_ABS:
                	state.pop();
                    state.push();
                    currentBlock.addStatement(new WasmAbs(module, codeEntry, ValueTypes.F64_TYPE));
                    break;
                case Instructions.F64_NEG:
                	state.pop();
                    state.push();
                    currentBlock.addStatement(new WasmNeg(module, codeEntry, ValueTypes.F64_TYPE));
                    break;
                case Instructions.F64_CEIL:
                	state.pop();
                    state.push();
                    currentBlock.addStatement(new WasmCeil(module, codeEntry, ValueTypes.F64_TYPE));
                    break;
                case Instructions.F64_FLOOR:
                	state.pop();
                    state.push();
                    currentBlock.addStatement(new WasmFloor(module, codeEntry, ValueTypes.F64_TYPE));
                    break;
                case Instructions.F64_TRUNC:
                	state.pop();
                    state.push();
                    currentBlock.addStatement(new WasmTrunc(module, codeEntry, ValueTypes.F64_TYPE));
                    break;
                case Instructions.F64_NEAREST:
                	state.pop();
                    state.push();
                    currentBlock.addStatement(new WasmNearest(module, codeEntry, ValueTypes.F64_TYPE));
                    break;
                case Instructions.F64_SQRT:
                    state.pop();
                    state.push();
                    currentBlock.addStatement(new WasmSqrt(module, codeEntry, ValueTypes.F64_TYPE));
                    break;
                case Instructions.F64_ADD:
                	state.pop();
                    state.pop();
                    state.push();
                    currentBlock.addStatement(new WasmAdd(module, codeEntry, ValueTypes.F64_TYPE));
                    break;
                case Instructions.F64_SUB:
                	state.pop();
                    state.pop();
                    state.push();
                    currentBlock.addStatement(new WasmSub(module, codeEntry, ValueTypes.F64_TYPE));
                    break;
                case Instructions.F64_MUL:
                	state.pop();
                    state.pop();
                    state.push();
                    currentBlock.addStatement(new WasmMul(module, codeEntry, ValueTypes.F64_TYPE));
                    break;
                case Instructions.F64_DIV:
                	state.pop();
                    state.pop();
                    state.push();
                    currentBlock.addStatement(new WasmDiv(module, codeEntry, ValueTypes.F64_TYPE));
                    break;
                case Instructions.F64_MIN:
                	state.pop();
                    state.pop();
                    state.push();
                    currentBlock.addStatement(new WasmMin(module, codeEntry, ValueTypes.F64_TYPE));
                    break;
                case Instructions.F64_MAX:
                	state.pop();
                    state.pop();
                    state.push();
                    currentBlock.addStatement(new WasmMax(module, codeEntry, ValueTypes.F64_TYPE));
                    break;
                case Instructions.F64_COPYSIGN:
                    state.pop();
                    state.pop();
                    state.push();
                    currentBlock.addStatement(new WasmCopySign(module, codeEntry, ValueTypes.F64_TYPE));
                    break;
                case Instructions.I32_WRAP_I64:
                	state.pop();
                    state.push();
                    currentBlock.addStatement(new WasmWrap(module, codeEntry));
                    break;
                case Instructions.I32_TRUNC_F32_S:
                case Instructions.I32_TRUNC_F32_U:
                	state.pop();
                    state.push();
                    currentBlock.addStatement(new WasmTruncConv(module, codeEntry, ValueTypes.F32_TYPE, ValueTypes.I32_TYPE));
                    break;
                case Instructions.I32_TRUNC_F64_S:
                case Instructions.I32_TRUNC_F64_U:
                	state.pop();
                    state.push();
                    currentBlock.addStatement(new WasmTruncConv(module, codeEntry, ValueTypes.F64_TYPE, ValueTypes.I32_TYPE));
                    break;
                case Instructions.I64_EXTEND_I32_S:
                case Instructions.I64_EXTEND_I32_U:
                	state.pop();
                    state.push();
                    currentBlock.addStatement(new WasmExtend(module, codeEntry, opcode == Instructions.I64_EXTEND_I32_S));
                    break;
                case Instructions.I64_TRUNC_F32_S:
                case Instructions.I64_TRUNC_F32_U:
                	state.pop();
                    state.push();
                    currentBlock.addStatement(new WasmTruncConv(module, codeEntry, ValueTypes.F32_TYPE, ValueTypes.I64_TYPE));
                    break;
                case Instructions.I64_TRUNC_F64_S:
                case Instructions.I64_TRUNC_F64_U:
                	state.pop();
                    state.push();
                    currentBlock.addStatement(new WasmTruncConv(module, codeEntry, ValueTypes.F64_TYPE, ValueTypes.I64_TYPE));
                    break;
                case Instructions.F32_CONVERT_I32_S:
                case Instructions.F32_CONVERT_I32_U:
                	state.pop();
                    state.push();
                    currentBlock.addStatement(new WasmConvert(module, codeEntry, ValueTypes.I32_TYPE, ValueTypes.F32_TYPE));
                    break;
                case Instructions.F32_CONVERT_I64_S:
                case Instructions.F32_CONVERT_I64_U:
                	state.pop();
                    state.push();
                    currentBlock.addStatement(new WasmConvert(module, codeEntry, ValueTypes.I64_TYPE, ValueTypes.F32_TYPE));
                    break;
                case Instructions.F32_DEMOTE_F64:
                	state.pop();
                    state.push();
                    currentBlock.addStatement(new WasmDemote(module, codeEntry));
                    break;
                case Instructions.F64_CONVERT_I32_S:
                case Instructions.F64_CONVERT_I32_U:
                	state.pop();
                    state.push();
                    currentBlock.addStatement(new WasmConvert(module, codeEntry, ValueTypes.I32_TYPE, ValueTypes.F64_TYPE));
                    break;
                case Instructions.F64_CONVERT_I64_S:
                case Instructions.F64_CONVERT_I64_U:
                	state.pop();
                    state.push();
                    currentBlock.addStatement(new WasmConvert(module, codeEntry, ValueTypes.I64_TYPE, ValueTypes.F64_TYPE));
                    break;
                case Instructions.F64_PROMOTE_F32:
                	state.pop();
                    state.push();
                    currentBlock.addStatement(new WasmPromote(module, codeEntry));
                    break;
                case Instructions.I32_REINTERPRET_F32:
                	state.pop();
                    state.push();
                    currentBlock.addStatement(new WasmReinterpret(module, codeEntry, ValueTypes.F32_TYPE, ValueTypes.I32_TYPE));
                    break;
                case Instructions.I64_REINTERPRET_F64:
                	state.pop();
                    state.push();
                    currentBlock.addStatement(new WasmReinterpret(module, codeEntry, ValueTypes.F64_TYPE, ValueTypes.I64_TYPE));
                    break;
                case Instructions.F32_REINTERPRET_I32:
                	state.pop();
                    state.push();
                    currentBlock.addStatement(new WasmReinterpret(module, codeEntry, ValueTypes.I32_TYPE, ValueTypes.F32_TYPE));
                    break;
                case Instructions.F64_REINTERPRET_I64:
                    state.pop();
                    state.push();
                    currentBlock.addStatement(new WasmReinterpret(module, codeEntry, ValueTypes.I64_TYPE, ValueTypes.F64_TYPE));
                    break;
                default:
                    Assert.fail(Assert.format("Unknown opcode: 0x%02x", opcode));
                    break;
            }
        } while (opcode != Instructions.END && opcode != Instructions.ELSE);
        // TODO: Restore this check, when we fix the case where the block contains a return
        // instruction.
        // checkValidStateOnBlockExit(returnTypeId, state, startStackSize);

        // Pop the current block return length in the return lengths stack.
        // Used when branching out of nested blocks (br and br_if instructions).
        state.popContinuationReturnLength();
        currentBlock.buildEffectiveStatements();
        return currentBlock;
    }

    private WasmLoopNode readLoop(WasmContext context, WasmCodeEntry codeEntry, ExecutionState state, byte returnTypeId) {
        int initialStackPointer = state.stackSize();
        WasmBlockNode loopBlock = readBlockBody(context, codeEntry, state, returnTypeId, ValueTypes.VOID_TYPE, false);
        WasmLoopNode loop = new WasmLoopNode(module, codeEntry, loopBlock);
        // TODO: Hack to correctly set the stack pointer for abstract interpretation.
        // If a block has branch instructions that target "shallower" blocks which return no value,
        // then it can leave no values in the stack, which is invalid for our abstract
        // interpretation.
        // Correct the stack pointer to the value it would have in case there were no branch
        // instructions.
        state.setStackSize(returnTypeId != ValueTypes.VOID_TYPE ? initialStackPointer + 1 : initialStackPointer);
        return loop;
        //return Truffle.getRuntime().createLoopNode((RepeatingNode) loop);
    }

    private WasmIfNode readIf(WasmContext context, WasmCodeEntry codeEntry, ExecutionState state) {
        byte blockTypeId = readBlockType();
        // Note: the condition value was already popped at this point.
        int stackSizeAfterCondition = state.stackSize();

        // Read true branch.
        int startOffset = offset();
        WasmBlockNode trueBranchBlock = readBlockBody(context, codeEntry, state, blockTypeId, blockTypeId, false);

        // If a block has branch instructions that target "shallower" blocks which return no value,
        // then it can leave no values in the stack, which is invalid for our abstract
        // interpretation.
        // Correct the stack pointer to the value it would have in case there were no branch
        // instructions.
        state.setStackSize(stackSizeAfterCondition);

        // Read false branch, if it exists.
        WasmNode falseBranchBlock;
        if (peek1(-1) == Instructions.ELSE) {
            falseBranchBlock = readBlockBody(context, codeEntry, state, blockTypeId, blockTypeId, false);
        } else {
            if (blockTypeId != ValueTypes.VOID_TYPE) {
                Assert.fail("An if statement without an else branch block cannot return values.");
            }
            falseBranchBlock = new WasmEmptyNode(module, codeEntry);
        }

        int stackSizeBeforeCondition = stackSizeAfterCondition + 1;
        return new WasmIfNode(module, codeEntry, trueBranchBlock, falseBranchBlock, blockTypeId, stackSizeBeforeCondition);
    }

    private void readElementSection(WasmContext context) {
        int numElements = readVectorLength();
        for (int elemSegmentId = 0; elemSegmentId != numElements; ++elemSegmentId) {
            int tableIndex = readUnsignedInt32();
            // At the moment, WebAssembly only supports one table instance, thus the only valid
            // table index is 0.
            Assert.assertIntEqual(tableIndex, 0, "Invalid table index");

            // Table offset expression must be a constant expression with result type i32.
            // https://webassembly.github.io/spec/core/syntax/modules.html#element-segments
            // https://webassembly.github.io/spec/core/valid/instructions.html#constant-expressions

            // Read the offset expression.
            byte instruction = read1();
            int offsetAddress = -1;
            int offsetGlobalIndex = -1;
            switch (instruction) {
                case Instructions.I32_CONST: {
                    offsetAddress = readSignedInt32();
                    readEnd();
                    break;
                }
                case Instructions.GLOBAL_GET: {
                    offsetGlobalIndex = readGlobalIndex();
                    readEnd();
                    break;
                }
                default: {
                    Assert.fail(String.format("Invalid instruction for table offset expression: 0x%02X", instruction));
                }
            }

            // Copy the contents, or schedule a linker task for this.
            int segmentLength = readVectorLength();
            final SymbolTable symbolTable = module.symbolTable();
            final Table table = symbolTable.table();
            if (table == null || offsetGlobalIndex == -1) {
                // Note: we do not check if the earlier element segments were executed,
                // and we do not try to execute the element segments in order,
                // as we do with data sections and the memory.
                // Instead, if any table element is written more than once, we report an error.
                // Thus, the order in which the element sections are loaded is not important
                // (also, I did not notice the toolchains overriding the same element slots,
                // or anything in the spec about that).
                WasmFunction[] elements = new WasmFunction[segmentLength];
                for (int index = 0; index != segmentLength; ++index) {
                    final int functionIndex = readFunctionIndex();
                    final WasmFunction function = symbolTable.function(functionIndex);
                    elements[index] = function;
                }
                context.linker().resolveElemSegment(context, module, elemSegmentId, offsetAddress, offsetGlobalIndex, segmentLength, elements);
            } else {
                table.ensureSizeAtLeast(offsetAddress + segmentLength);
                for (int index = 0; index != segmentLength; ++index) {
                    final int functionIndex = readFunctionIndex();
                    final WasmFunction function = symbolTable.function(functionIndex);
                    table.set(offsetAddress + index, function);
                }
            }
        }
    }

    private void readEnd() {
        byte instruction = read1();
        Assert.assertByteEqual(instruction, (byte) Instructions.END, "Initialization expression must end with an END");
    }

    private void readStartSection() {
        int startFunctionIndex = readFunctionIndex();
        module.symbolTable().setStartFunction(startFunctionIndex);
    }

    private void readExportSection(WasmContext context) {
        int numExports = readVectorLength();
        for (int i = 0; i != numExports; ++i) {
            String exportName = readName();
            byte exportType = readExportType();
            switch (exportType) {
                case ExportIdentifier.FUNCTION: {
                    int functionIndex = readFunctionIndex();
                    module.symbolTable().exportFunction(context, functionIndex, exportName);
                    break;
                }
                case ExportIdentifier.TABLE: {
                    int tableIndex = readTableIndex();
                    Assert.assertTrue(module.symbolTable().tableExists(), "No table was imported or declared, so cannot export a table");
                    Assert.assertIntEqual(tableIndex, 0, "Cannot export table index different than zero (only one table per module allowed)");
                    module.symbolTable().exportTable(context, exportName);
                    break;
                }
                case ExportIdentifier.MEMORY: {
                    readMemoryIndex();
                    module.symbolTable().exportMemory(context, exportName);
                    break;
                }
                case ExportIdentifier.GLOBAL: {
                    int index = readGlobalIndex();
                    module.symbolTable().exportGlobal(context, exportName, index);
                    break;
                }
                default: {
                    Assert.fail(String.format("Invalid export type identifier: 0x%02X", exportType));
                }
            }
        }
    }

    private void readGlobalSection(WasmContext context) {
        final GlobalRegistry globals = context.globals();
        int numGlobals = readVectorLength();
        int startingGlobalIndex = module.symbolTable().maxGlobalIndex() + 1;
        for (int globalIndex = startingGlobalIndex; globalIndex != startingGlobalIndex + numGlobals; globalIndex++) {
            byte type = readValueType();
            // 0x00 means const, 0x01 means var
            byte mutability = readMutability();
            long value = 0;
            int existingIndex = -1;
            byte instruction = read1();
            boolean isInitialized;
            // Global initialization expressions must be constant expressions:
            // https://webassembly.github.io/spec/core/valid/instructions.html#constant-expressions
            switch (instruction) {
                case Instructions.I32_CONST:
                    value = readSignedInt32();
                    isInitialized = true;
                    break;
                case Instructions.I64_CONST:
                    value = readSignedInt64();
                    isInitialized = true;
                    break;
                case Instructions.F32_CONST:
                    value = readFloatAsInt32();
                    isInitialized = true;
                    break;
                case Instructions.F64_CONST:
                    value = readFloatAsInt64();
                    isInitialized = true;
                    break;
                case Instructions.GLOBAL_GET:
                    existingIndex = readGlobalIndex();
                    isInitialized = false;
                    break;
                default:
                    throw Assert.fail(String.format("Invalid instruction for global initialization: 0x%02X", instruction));
            }
            instruction = read1();
            Assert.assertByteEqual(instruction, (byte) Instructions.END, "Global initialization must end with END");
            final int address = module.symbolTable().declareGlobal(context, globalIndex, type, mutability);
            if (isInitialized) {
                globals.storeLong(address, value);
                context.linker().resolveGlobalInitialization(module, globalIndex);
            } else {
                if (!module.symbolTable().importedGlobals().containsKey(existingIndex)) {
                    // The current WebAssembly spec says constant expressions can only refer to
                    // imported globals. We can easily remove this restriction in the future.
                    Assert.fail("The initializer for global " + globalIndex + " in module '" + module.name() +
                                    "' refers to a non-imported global.");
                }
                context.linker().resolveGlobalInitialization(context, module, globalIndex, existingIndex);
            }
        }
    }

    private void readDataSection(WasmContext context) {
        int numDataSegments = readVectorLength();
        boolean allDataSectionsResolved = true;
        for (int dataSegmentId = 0; dataSegmentId != numDataSegments; ++dataSegmentId) {
            int memIndex = readUnsignedInt32();
            // At the moment, WebAssembly only supports one memory instance, thus the only valid
            // memory index is 0.
            Assert.assertIntEqual(memIndex, 0, "Invalid memory index, only the memory index 0 is currently supported.");
            byte instruction = read1();

            // Data dataOffset expression must be a constant expression with result type i32.
            // https://webassembly.github.io/spec/core/syntax/modules.html#data-segments
            // https://webassembly.github.io/spec/core/valid/instructions.html#constant-expressions

            // Read the offset expression.
            int offsetAddress = -1;
            int offsetGlobalIndex = -1;
            switch (instruction) {
                case Instructions.I32_CONST:
                    offsetAddress = readSignedInt32();
                    readEnd();
                    break;
                case Instructions.GLOBAL_GET:
                    offsetGlobalIndex = readGlobalIndex();
                    readEnd();
                    break;
                default:
                    Assert.fail(String.format("Invalid instruction for data offset expression: 0x%02X", instruction));
            }
            // Try to immediately resolve the global's value, if that global is initialized.
            // Test functions that re-read the data section to reset the memory depend on this,
            // since they need to avoid re-linking.
            if (offsetGlobalIndex != -1 && module.symbolTable().isGlobalInitialized(offsetGlobalIndex)) {
                int offsetGlobalAddress = module.symbolTable().globalAddress(offsetGlobalIndex);
                offsetAddress = context.globals().loadAsInt(offsetGlobalAddress);
                offsetGlobalIndex = -1;
            }

            // Copy the contents, or schedule a linker task for this.
            int byteLength = readVectorLength();
            final WasmMemory memory = module.symbolTable().memory();
            if (memory == null || !allDataSectionsResolved || offsetGlobalIndex != -1) {
                // A data section can only be resolved after the memory is resolved.
                // If the data section is offset by a global variable,
                // then the data section can only be resolved after the global is resolved.
                // When some data section is not resolved, all the later data sections must be
                // resolved after it.
                byte[] dataSegment = new byte[byteLength];
                for (int writeOffset = 0; writeOffset != byteLength; ++writeOffset) {
                    byte b = read1();
                    dataSegment[writeOffset] = b;
                }
                context.linker().resolveDataSegment(context, module, dataSegmentId, offsetAddress, offsetGlobalIndex, byteLength, dataSegment, allDataSectionsResolved);
                allDataSectionsResolved = false;
            } else {
                // A data section can be loaded directly into memory only if there are no prior
                // unresolved data sections.
                memory.validateAddress(null, offsetAddress, byteLength);
                for (int writeOffset = 0; writeOffset != byteLength; ++writeOffset) {
                    byte b = read1();
                    memory.store_i32_8(null, offsetAddress + writeOffset, b);
                }
            }
        }
    }

    private void readFunctionType() {
        int paramsLength = readVectorLength();
        int resultLength = peekUnsignedInt32(paramsLength);
        resultLength = (resultLength == 0x40) ? 0 : resultLength;
        int idx = module.symbolTable().allocateFunctionType(paramsLength, resultLength);
        readParameterList(idx, paramsLength);
        readResultList(idx);
    }

    private void readParameterList(int funcTypeIdx, int numParams) {
        for (int paramIdx = 0; paramIdx != numParams; ++paramIdx) {
            byte type = readValueType();
            module.symbolTable().registerFunctionTypeParameterType(funcTypeIdx, paramIdx, type);
        }
    }

    // Specification seems ambiguous:
    // https://webassembly.github.io/spec/core/binary/types.html#result-types
    // According to the spec, the result type can only be 0x40 (void) or 0xtt, where tt is a value
    // type.
    // However, the Wasm binary compiler produces binaries with either 0x00 or 0x01 0xtt. Therefore,
    // we support both.
    private void readResultList(int funcTypeIdx) {
        byte b = read1();
        switch (b) {
            case ValueTypes.VOID_TYPE:  // special byte indicating empty return type (same as above)
                break;
            case 0x00:  // empty vector
                break;
            case 0x01:  // vector with one element (produced by the Wasm binary compiler)
                byte type = readValueType();
                module.symbolTable().registerFunctionTypeReturnType(funcTypeIdx, 0, type);
                break;
            default:
                Assert.fail(String.format("Invalid return value specifier: 0x%02X", b));
        }
    }

    private boolean isEOF() {
        return offset == data.length;
    }

    private int readVectorLength() {
        return readUnsignedInt32();
    }

    private int readFunctionIndex() {
        return readUnsignedInt32();
    }

    private int readTypeIndex() {
        return readUnsignedInt32();
    }

    private int readTypeIndex(byte[] bytesConsumedResult) {
        return readUnsignedInt32(bytesConsumedResult);
    }

    private int readFunctionIndex(byte[] bytesConsumedResult) {
        return readUnsignedInt32(bytesConsumedResult);
    }

    private int readTableIndex() {
        return readUnsignedInt32();
    }

    private int readMemoryIndex() {
        return readUnsignedInt32();
    }

    private int readGlobalIndex() {
        return readUnsignedInt32();
    }

    @SuppressWarnings("unused")
    private int readLocalIndex() {
        return readUnsignedInt32();
    }

    private int readLocalIndex(byte[] bytesConsumedResult) {
        return readUnsignedInt32(bytesConsumedResult);
    }

    private int readLabelIndex() {
        return readUnsignedInt32();
    }

    private int readLabelIndex(byte[] bytesConsumedResult) {
        return readUnsignedInt32(bytesConsumedResult);
    }

    private byte readExportType() {
        return read1();
    }

    private byte readImportType() {
        return read1();
    }

    private byte readElemType() {
        return read1();
    }

    private byte readLimitsPrefix() {
        return read1();
    }

    private String readName() {
        int nameLength = readVectorLength();
        byte[] name = new byte[nameLength];
        for (int i = 0; i != nameLength; ++i) {
            name[i] = read1();
        }
        return new String(name, StandardCharsets.US_ASCII);
    }

    private boolean tryJumpToSection(int targetSectionId) {
        offset = 0;
        validateMagicNumberAndVersion();
        while (!isEOF()) {
            byte sectionID = read1();
            int size = readUnsignedInt32();
            if (sectionID == targetSectionId) {
                return true;
            }
            offset += size;
        }
        return false;
    }

    /**
     * Reset the state of the globals in a module that had already been parsed and linked.
     */
    @SuppressWarnings("unused")
    void resetGlobalState(WasmContext context) {
        int globalIndex = 0;
        if (tryJumpToSection(Section.IMPORT)) {
            int numImports = readVectorLength();
            for (int i = 0; i != numImports; ++i) {
                String moduleName = readName();
                String memberName = readName();
                byte importType = readImportType();
                switch (importType) {
                    case ImportIdentifier.FUNCTION: {
                        readTableIndex();
                        break;
                    }
                    case ImportIdentifier.TABLE: {
                        readElemType();
                        byte limitsPrefix = read1();
                        switch (limitsPrefix) {
                            case LimitsPrefix.NO_MAX: {
                                readUnsignedInt32();
                                break;
                            }
                            case LimitsPrefix.WITH_MAX: {
                                readUnsignedInt32();
                                readUnsignedInt32();
                                break;
                            }
                        }
                        break;
                    }
                    case ImportIdentifier.MEMORY: {
                        byte limitsPrefix = read1();
                        switch (limitsPrefix) {
                            case LimitsPrefix.NO_MAX: {
                                readUnsignedInt32();
                                break;
                            }
                            case LimitsPrefix.WITH_MAX: {
                                readUnsignedInt32();
                                readUnsignedInt32();
                                break;
                            }
                        }
                        break;
                    }
                    case ImportIdentifier.GLOBAL: {
                        readValueType();
                        byte mutability = readMutability();
                        if (mutability == GlobalModifier.MUTABLE) {
                            throw new WasmLinkerException("Cannot reset imports of mutable global variables (not implemented).");
                        }
                        globalIndex++;
                        break;
                    }
                    default: {
                        // The module should have been parsed already.
                    }
                }
            }
        }
        if (tryJumpToSection(Section.GLOBAL)) {
            final GlobalRegistry globals = context.globals();
            int numGlobals = readVectorLength();
            int startingGlobalIndex = globalIndex;
            for (; globalIndex != startingGlobalIndex + numGlobals; globalIndex++) {
                readValueType();
                // Read mutability;
                read1();
                byte instruction = read1();
                long value = 0;
                switch (instruction) {
                    case Instructions.I32_CONST: {
                        value = readSignedInt32();
                        break;
                    }
                    case Instructions.I64_CONST: {
                        value = readSignedInt64();
                        break;
                    }
                    case Instructions.F32_CONST: {
                        value = readFloatAsInt32();
                        break;
                    }
                    case Instructions.F64_CONST: {
                        value = readFloatAsInt64();
                        break;
                    }
                    case Instructions.GLOBAL_GET: {
                        int existingIndex = readGlobalIndex();
                        if (module.symbolTable().globalMutability(existingIndex) == GlobalModifier.MUTABLE) {
                            throw new WasmLinkerException("Cannot reset global variables that were initialized " +
                                            "with a non-constant global variable (not implemented).");
                        }
                        final int existingAddress = module.symbolTable().globalAddress(existingIndex);
                        value = globals.loadAsLong(existingAddress);
                        break;
                    }
                }
                // Read END.
                read1();
                final int address = module.symbolTable().globalAddress(globalIndex);
                globals.storeLong(address, value);
            }
        }
    }

    void resetMemoryState(WasmContext context, boolean zeroMemory) {
        final WasmMemory memory = module.symbolTable().memory();
        if (memory != null && zeroMemory) {
            memory.clear();
        }
        if (tryJumpToSection(Section.DATA)) {
            readDataSection(context);
        }
    }
}
