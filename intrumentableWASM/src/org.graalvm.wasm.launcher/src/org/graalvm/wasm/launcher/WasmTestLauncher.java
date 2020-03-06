package org.graalvm.wasm.launcher;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;

import org.graalvm.launcher.AbstractLanguageLauncher;
import org.graalvm.options.OptionCategory;
import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.Source;
import org.graalvm.polyglot.Value;

public class WasmTestLauncher extends AbstractLanguageLauncher {

	private File file;
	static private String type;

    public static void main(String[] args) {
    	int errors = 0;
    	List<String> errorsFile = new ArrayList<>();
    	List<String> wasm = getFiles("/home/ALE/graal/intrumentableWASM/test/emcc/");
		for (String path : wasm) {
			String[] arg = {path};
			System.out.println("Testing : " + path);
			
			String expected = "ERROR";
			try {
				expected = new String ( Files.readAllBytes( Paths.get(path.replace(".wasm", ".result")) ) );
				type = expected.split(" ")[0];
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			System.out.println("expect : " + expected);
			
			try {
				new WasmTestLauncher().launch(arg);
			} catch (Exception e) {
				System.out.println("ERROR : an exception has been thrown");
				errors++;
				errorsFile.add(path);
			}
			
			
		}
		System.out.println();
		System.out.println("Test Report :");
		System.out.println("Number of tests : " + wasm.size());
		System.out.println("Number of errors : " + errors);
		System.out.println();
		System.out.println("Tests that failed :");
		for (String f : errorsFile) {
			System.out.println("\t"+f);
		}
    }

    @Override
    protected List<String> preprocessArguments(List<String> arguments, Map<String, String> polyglotOptions) {
        final ListIterator<String> argIterator = arguments.listIterator();
        final ArrayList<String> unrecognizedArguments = new ArrayList<>();
        while (argIterator.hasNext()) {
            final String argument = argIterator.next();
            if (argument.startsWith("-")) {
                unrecognizedArguments.add(argument);
            } else {
                file = new File(argument);
                if (!file.exists()) {
                    throw abort(String.format("WebAssembly binary '%s' does not exist.", file));
                }
                if (argIterator.hasNext()) {
                    throw abort("No options are allowed after the binary name.");
                }
            }
        }
        if (file == null) {
            throw abort("Must specify the binary name.");
        }
        return unrecognizedArguments;
    }

    @Override
    protected void launch(Context.Builder contextBuilder) {
        execute(contextBuilder);
    }

    private int execute(Context.Builder contextBuilder) {
        try (Context context = contextBuilder.build()) {
            context.eval(Source.newBuilder(getLanguageId(), file).build());
            // Currently, the spec does not commit to a name for the entry points.
            // We speculate on the possible exported name.
            Value entryPoint = context.getBindings(getLanguageId()).getMember("main");
            if (entryPoint == null) {
                // Try the Emscripten naming convention.
                entryPoint = context.getBindings(getLanguageId()).getMember("_main");
            }
            if (entryPoint == null) {
                // Try the wasi-sdk naming convention.
                entryPoint = context.getBindings(getLanguageId()).getMember("_start");
            }
            if (entryPoint == null) {
                throw abort("No entry-point function found, cannot start program.");
            }
            
            if (type.equals("int")) {
            	int val = entryPoint.execute().asInt();
                System.out.println("value  : int " + val);
			} else if (type.equals("double")) {
            	double val = entryPoint.execute().asDouble();
                System.out.println("value  : double " + val);
			} else if (type.equals("long")) {
            	long val = entryPoint.execute().asLong();
                System.out.println("value  : long " + val);
			} else if (type.equals("float")) {
            	float val = entryPoint.execute().asFloat();
                System.out.println("value  : float " + val);
			}
            
            return 0;
        } catch (IOException e) {
            throw abort(String.format("Error loading file '%s': %s", file, e.getMessage()));
        }
    }
    
    private static List<String> getFiles(String folder) {

		List<String> list = new ArrayList<String>();
        File dir = new File(folder);
        if(dir.isDirectory()) {
            FileFilter wasm = new FileFilter() {

                public boolean accept(File file) {
                    if(file.isFile() && !file.isDirectory()) {
                        String filename = file.getName();
                        return filename.endsWith("fib.wasm");
                    }
					return false;
                }

            };
            FileFilter folderFilter = new FileFilter() {
                public boolean accept(File file) {
					return file.isDirectory();
                }
            };
            File[] dirs = dir.listFiles(folderFilter);
            File[] fileNames = dir.listFiles(wasm);
            for (File file : fileNames) {
                list.add(file.getAbsolutePath());
            }
            for (File d : dirs) {
				list.addAll(getFiles(d.getAbsolutePath()));
			}
        }
        return list;

	}

    @Override
    protected String getLanguageId() {
        return "wasm";
    }

    @Override
    protected void printHelp(OptionCategory maxCategory) {
        System.out.println();
        System.out.println("Usage: wasm [OPTION]... [FILE]");
        System.out.println("Run WebAssembly binary files on GraalVM's wasm engine.");
    }

    @Override
    protected void collectArguments(Set<String> options) {
    }

}
