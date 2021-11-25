package gloomyfolken.hooklib.minecraft;

import gloomyfolken.hooklib.asm.AsmHook;
import gloomyfolken.hooklib.asm.HookClassTransformer;
import gloomyfolken.hooklib.asm.HookInjectorClassVisitor;
import net.minecraft.launchwrapper.IClassTransformer;
import org.objectweb.asm.ClassWriter;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static gloomyfolken.hooklib.asm.HookLoggerManager.getLogger;

/**
 * Этот трансформер занимается вставкой хуков с момента запуска майнкрафта. Здесь сосредоточены все костыли,
 * которые необходимы для правильной работы с обфусцированными названиями методов.
 */
@SuppressWarnings("unused")
public class MinecraftClassTransformer extends HookClassTransformer implements IClassTransformer {

    static MinecraftClassTransformer instance;
    private Map<Integer, String> methodNames;

    private static final List<IClassTransformer> postTransformers = new ArrayList<>();

    public MinecraftClassTransformer() {
        instance = this;

        if (HookLibPlugin.getObfuscated()) {
            try {
                long timeStart = System.currentTimeMillis();
                methodNames = loadMethodNames();
                long time = System.currentTimeMillis() - timeStart;
                getLogger().debug("Methods dictionary loaded in " + time + " ms");
            } catch (IOException e) {
                getLogger().severe("Can not load obfuscated method names", e);
            }
        }

        this.classMetadataReader = HookLoader.getDeobfuscationMetadataReader();

        this.hooksMap.putAll(PrimaryClassTransformer.instance.getHooksMap());
        PrimaryClassTransformer.instance.getHooksMap().clear();
        PrimaryClassTransformer.instance.registeredSecondTransformer = true;
    }

    private HashMap<Integer, String> loadMethodNames() throws IOException {
        InputStream resourceStream = getClass().getResourceAsStream("/methods.bin");
        if (resourceStream == null) throw new IOException("Methods dictionary not found");
        DataInputStream input = new DataInputStream(new BufferedInputStream(resourceStream));
        int numMethods = input.readInt();
        HashMap<Integer, String> map = new HashMap<>(numMethods);
        for (int i = 0; i < numMethods; i++) {
            map.put(input.readInt(), input.readUTF());
        }
        input.close();
        return map;
    }

    @Override
    public byte[] transform(String oldName, String newName, byte[] bytecode) {
        bytecode = transform(newName, bytecode);
        for (IClassTransformer postTransformer : postTransformers) {
            bytecode = postTransformer.transform(oldName, newName, bytecode);
        }
        return bytecode;
    }

    @Override
    protected HookInjectorClassVisitor createInjectorClassVisitor(ClassWriter cw, List<AsmHook> hooks) {
        return new HookInjectorClassVisitor(this, cw, hooks) {
            @Override
            protected boolean isTargetMethod(AsmHook hook, String name, String desc) {
                if (HookLibPlugin.getObfuscated()) {
                    String mcpName = methodNames.get(getMethodId(name));
                    if (mcpName != null && super.isTargetMethod(hook, mcpName, desc)) {
                        return true;
                    }
                }
                return super.isTargetMethod(hook, name, desc);
            }
        };
    }

    public Map<Integer, String> getMethodNames() {
        return methodNames;
    }

    public static int getMethodId(String srgName) {
        if (srgName.startsWith("func_")) {
            int first = srgName.indexOf('_');
            int second = srgName.indexOf('_', first + 1);
            return Integer.parseInt(srgName.substring(first + 1, second));
        } else {
            return -1;
        }
    }

    /**
     * Регистрирует трансформер, который будет запущен после обычных, и в том числе после деобфусцирующего трансформера.
     */
    public static void registerPostTransformer(IClassTransformer transformer) {
        postTransformers.add(transformer);
    }
}
