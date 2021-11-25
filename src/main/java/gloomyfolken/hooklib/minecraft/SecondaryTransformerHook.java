package gloomyfolken.hooklib.minecraft;

import cpw.mods.fml.common.Loader;
import gloomyfolken.hooklib.asm.Hook;
import net.minecraft.launchwrapper.LaunchClassLoader;

import static gloomyfolken.hooklib.asm.HookLoggerManager.getLogger;

@SuppressWarnings("unused")
public class SecondaryTransformerHook {

    /**
     * Регистрирует хук-трансформер последним.
     */
    @Hook
    public static void injectData(Loader loader, Object... data) {
        ClassLoader classLoader = SecondaryTransformerHook.class.getClassLoader();
        if (classLoader instanceof LaunchClassLoader) {
            ((LaunchClassLoader) classLoader).registerTransformer(MinecraftClassTransformer.class.getName());
        } else {
            getLogger().severe("HookLib was not loaded by LaunchClassLoader. Hooks will not be injected.");
        }
    }

}
