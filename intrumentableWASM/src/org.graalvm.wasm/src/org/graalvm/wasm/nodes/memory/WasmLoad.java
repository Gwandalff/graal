package org.graalvm.wasm.nodes.memory;

import static org.graalvm.wasm.constants.Instructions.F32_LOAD;
import static org.graalvm.wasm.constants.Instructions.F64_LOAD;
import static org.graalvm.wasm.constants.Instructions.I32_LOAD;
import static org.graalvm.wasm.constants.Instructions.I32_LOAD16_S;
import static org.graalvm.wasm.constants.Instructions.I32_LOAD16_U;
import static org.graalvm.wasm.constants.Instructions.I32_LOAD8_S;
import static org.graalvm.wasm.constants.Instructions.I32_LOAD8_U;
import static org.graalvm.wasm.constants.Instructions.I64_LOAD;
import static org.graalvm.wasm.constants.Instructions.I64_LOAD16_S;
import static org.graalvm.wasm.constants.Instructions.I64_LOAD16_U;
import static org.graalvm.wasm.constants.Instructions.I64_LOAD32_S;
import static org.graalvm.wasm.constants.Instructions.I64_LOAD32_U;
import static org.graalvm.wasm.constants.Instructions.I64_LOAD8_S;
import static org.graalvm.wasm.constants.Instructions.I64_LOAD8_U;

import org.graalvm.wasm.WasmCodeEntry;
import org.graalvm.wasm.WasmContext;
import org.graalvm.wasm.WasmModule;
import org.graalvm.wasm.constants.TargetOffset;
import org.graalvm.wasm.exception.WasmTrap;
import org.graalvm.wasm.memory.WasmMemory;
import org.graalvm.wasm.memory.WasmMemoryException;
import org.graalvm.wasm.nodes.WasmNode;

import com.oracle.truffle.api.CompilerDirectives.CompilationFinal;
import com.oracle.truffle.api.frame.VirtualFrame;

public class WasmLoad extends WasmNode {
	
	@CompilationFinal private final int memOffset;
	@CompilationFinal private final int loadType;

	public WasmLoad(WasmModule wasmModule, WasmCodeEntry codeEntry, int loadType, int memOffset) {
		super(wasmModule, codeEntry);
		this.loadType = loadType;
		this.memOffset = memOffset;
	}

	@Override
	public TargetOffset execute(WasmContext context, VirtualFrame frame) {
		context.stackpointer--;
        int baseAddress = popInt(frame, context.stackpointer);
        int address = baseAddress + memOffset;
        WasmMemory memory = module().symbolTable().memory();
        
        try {
            switch (loadType) {
                case I32_LOAD: {
                    int value = memory.load_i32(this, address);
                    pushInt(frame, context.stackpointer, value);
                    break;
                }
                case I64_LOAD: {
                    long value = memory.load_i64(this, address);
                    push(frame, context.stackpointer, value);
                    break;
                }
                case F32_LOAD: {
                    float value = memory.load_f32(this, address);
                    pushFloat(frame, context.stackpointer, value);
                    break;
                }
                case F64_LOAD: {
                    double value = memory.load_f64(this, address);
                    pushDouble(frame, context.stackpointer, value);
                    break;
                }
                case I32_LOAD8_S: {
                    int value = memory.load_i32_8s(this, address);
                    pushInt(frame, context.stackpointer, value);
                    break;
                }
                case I32_LOAD8_U: {
                    int value = memory.load_i32_8u(this, address);
                    pushInt(frame, context.stackpointer, value);
                    break;
                }
                case I32_LOAD16_S: {
                    int value = memory.load_i32_16s(this, address);
                    pushInt(frame, context.stackpointer, value);
                    break;
                }
                case I32_LOAD16_U: {
                    int value = memory.load_i32_16u(this, address);
                    pushInt(frame, context.stackpointer, value);
                    break;
                }
                case I64_LOAD8_S: {
                    long value = memory.load_i64_8s(this, address);
                    push(frame, context.stackpointer, value);
                    break;
                }
                case I64_LOAD8_U: {
                    long value = memory.load_i64_8u(this, address);
                    push(frame, context.stackpointer, value);
                    break;
                }
                case I64_LOAD16_S: {
                    long value = memory.load_i64_16s(this, address);
                    push(frame, context.stackpointer, value);
                    break;
                }
                case I64_LOAD16_U: {
                    long value = memory.load_i64_16u(this, address);
                    push(frame, context.stackpointer, value);
                    break;
                }
                case I64_LOAD32_S: {
                    long value = memory.load_i64_32s(this, address);
                    push(frame, context.stackpointer, value);
                    break;
                }
                case I64_LOAD32_U: {
                    long value = memory.load_i64_32u(this, address);
                    push(frame, context.stackpointer, value);
                    break;
                }
                default: {
                    throw new WasmTrap(this, "Unknown load type: " + loadType);
                }
            }
        } catch (WasmMemoryException e) {
            throw new WasmTrap(this, "memory address out-of-bounds");
        }
        context.stackpointer++;
        
		return null;
	}

	@Override
	public byte returnTypeId() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int byteConstantLength() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int intConstantLength() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int longConstantLength() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int branchTableLength() {
		// TODO Auto-generated method stub
		return 0;
	}

}
