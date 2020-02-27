package org.graalvm.wasm.nodes.variables;

import static org.graalvm.wasm.WasmTracing.trace;

import org.graalvm.wasm.ValueTypes;
import org.graalvm.wasm.WasmCodeEntry;
import org.graalvm.wasm.WasmContext;
import org.graalvm.wasm.WasmModule;
import org.graalvm.wasm.constants.TargetOffset;
import org.graalvm.wasm.exception.WasmTrap;
import org.graalvm.wasm.nodes.WasmNode;

import com.oracle.truffle.api.CompilerDirectives.CompilationFinal;
import com.oracle.truffle.api.frame.VirtualFrame;

public class WasmLocalGet extends WasmNode {
	
	@CompilationFinal private final byte type;
	@CompilationFinal private final int intvalue;
	@CompilationFinal private final long longvalue;
	@CompilationFinal private final float floatvalue;
	@CompilationFinal private final double doublevalue;

	public WasmLocalGet(WasmModule wasmModule, WasmCodeEntry codeEntry, int byteLength, int value) {
		super(wasmModule, codeEntry, byteLength);
		this.type = ValueTypes.I32_TYPE;
		this.intvalue = value;
		this.longvalue = 0;
		this.floatvalue = 0;
		this.doublevalue = 0;
	}
	public WasmLocalGet(WasmModule wasmModule, WasmCodeEntry codeEntry, int byteLength, long value) {
		super(wasmModule, codeEntry, byteLength);
		this.type = ValueTypes.I64_TYPE;
		this.intvalue = 0;
		this.longvalue = value;
		this.floatvalue = 0;
		this.doublevalue = 0;
	}
	public WasmLocalGet(WasmModule wasmModule, WasmCodeEntry codeEntry, int byteLength, float value) {
		super(wasmModule, codeEntry, byteLength);
		this.type = ValueTypes.F32_TYPE;
		this.intvalue = 0;
		this.longvalue = 0;
		this.floatvalue = value;
		this.doublevalue = 0;
	}
	public WasmLocalGet(WasmModule wasmModule, WasmCodeEntry codeEntry, int byteLength, double value) {
		super(wasmModule, codeEntry, byteLength);
		this.type = ValueTypes.F64_TYPE;
		this.intvalue = 0;
		this.longvalue = 0;
		this.floatvalue = 0;
		this.doublevalue = value;
	}

	@Override
	public TargetOffset execute(WasmContext context, VirtualFrame frame) {
		int index = codeEntry().longConstantAsInt(longConstantOffset);
        longConstantOffset++;
        byte constantLength = codeEntry().byteConstant(byteConstantOffset);
        byteConstantOffset++;
        offset += constantLength;
        byte type = codeEntry().localType(index);
        switch (type) {
            case ValueTypes.I32_TYPE: {
                int value = getInt(frame, index);
                pushInt(frame, stackPointer, value);
                stackPointer++;
                trace("local.get %d, value = 0x%08X (%d) [i32]", index, value, value);
                break;
            }
            case ValueTypes.I64_TYPE: {
                long value = getLong(frame, index);
                push(frame, stackPointer, value);
                stackPointer++;
                trace("local.get %d, value = 0x%016X (%d) [i64]", index, value, value);
                break;
            }
            case ValueTypes.F32_TYPE: {
                float value = getFloat(frame, index);
                pushFloat(frame, stackPointer, value);
                stackPointer++;
                trace("local.get %d, value = %f [f32]", index, value);
                break;
            }
            case ValueTypes.F64_TYPE: {
                double value = getDouble(frame, index);
                pushDouble(frame, stackPointer, value);
                stackPointer++;
                trace("local.get %d, value = %f [f64]", index, value);
                break;
            }
            default: {
                throw new WasmTrap(this, "Local variable cannot have the void type.");
            }
        }
        break;
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
