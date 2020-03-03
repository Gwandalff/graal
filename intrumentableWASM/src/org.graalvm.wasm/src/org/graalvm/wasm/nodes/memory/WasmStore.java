package org.graalvm.wasm.nodes.memory;

import static org.graalvm.wasm.constants.Instructions.F32_STORE;
import static org.graalvm.wasm.constants.Instructions.F64_STORE;
import static org.graalvm.wasm.constants.Instructions.I32_STORE;
import static org.graalvm.wasm.constants.Instructions.I32_STORE_16;
import static org.graalvm.wasm.constants.Instructions.I32_STORE_8;
import static org.graalvm.wasm.constants.Instructions.I64_STORE;
import static org.graalvm.wasm.constants.Instructions.I64_STORE_16;
import static org.graalvm.wasm.constants.Instructions.I64_STORE_32;
import static org.graalvm.wasm.constants.Instructions.I64_STORE_8;

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

public class WasmStore extends WasmNode {
	
	@CompilationFinal private final int memOffset;
	@CompilationFinal private final int storeType;

	public WasmStore(WasmModule wasmModule, WasmCodeEntry codeEntry, int storeType, int memOffset) {
		super(wasmModule, codeEntry);
		this.memOffset = memOffset;
		this.storeType = storeType;
	}

	@Override
	public TargetOffset execute(WasmContext context, VirtualFrame frame) {
		WasmMemory memory = module().symbolTable().memory();

        try {
            switch (storeType) {
                case I32_STORE: {
                    context.stackpointer--;
                    int value = popInt(frame, context.stackpointer);
                    context.stackpointer--;
                    int baseAddress = popInt(frame, context.stackpointer);
                    int address = baseAddress + memOffset;
                    memory.store_i32(this, address, value);
                    break;
                }
                case I64_STORE: {
                    context.stackpointer--;
                    long value = pop(frame, context.stackpointer);
                    context.stackpointer--;
                    int baseAddress = popInt(frame, context.stackpointer);
                    int address = baseAddress + memOffset;
                    memory.store_i64(this, address, value);
                    break;
                }
                case F32_STORE: {
                    context.stackpointer--;
                    float value = popAsFloat(frame, context.stackpointer);
                    context.stackpointer--;
                    int baseAddress = popInt(frame, context.stackpointer);
                    int address = baseAddress + memOffset;
                    memory.store_f32(this, address, value);
                    break;
                }
                case F64_STORE: {
                    context.stackpointer--;
                    double value = popAsDouble(frame, context.stackpointer);
                    context.stackpointer--;
                    int baseAddress = popInt(frame, context.stackpointer);
                    int address = baseAddress + memOffset;
                    memory.store_f64(this, address, value);
                    break;
                }
                case I32_STORE_8: {
                    context.stackpointer--;
                    int value = popInt(frame, context.stackpointer);
                    context.stackpointer--;
                    int baseAddress = popInt(frame, context.stackpointer);
                    int address = baseAddress + memOffset;
                    memory.store_i32_8(this, address, (byte) value);
                    break;
                }
                case I32_STORE_16: {
                    context.stackpointer--;
                    int value = popInt(frame, context.stackpointer);
                    context.stackpointer--;
                    int baseAddress = popInt(frame, context.stackpointer);
                    int address = baseAddress + memOffset;
                    memory.store_i32_16(this, address, (short) value);
                    break;
                }
                case I64_STORE_8: {
                    context.stackpointer--;
                    long value = pop(frame, context.stackpointer);
                    context.stackpointer--;
                    int baseAddress = popInt(frame, context.stackpointer);
                    int address = baseAddress + memOffset;
                    memory.store_i64_8(this, address, (byte) value);
                    break;
                }
                case I64_STORE_16: {
                    context.stackpointer--;
                    long value = pop(frame, context.stackpointer);
                    context.stackpointer--;
                    int baseAddress = popInt(frame, context.stackpointer);
                    int address = baseAddress + memOffset;
                    memory.store_i64_16(this, address, (short) value);
                    break;
                }
                case I64_STORE_32: {
                    context.stackpointer--;
                    long value = pop(frame, context.stackpointer);
                    context.stackpointer--;
                    int baseAddress = popInt(frame, context.stackpointer);
                    int address = baseAddress + memOffset;
                    memory.store_i64_32(this, address, (int) value);
                    break;
                }
                default: {
                    throw new WasmTrap(this, "Unknown store type: " + storeType);
                }
            }
        } catch (WasmMemoryException e) {
            throw new WasmTrap(this, "memory address out-of-bounds");
        }
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
