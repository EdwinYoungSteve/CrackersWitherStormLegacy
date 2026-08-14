package com.wdcftgg.witherstormmod.common.init;

import net.minecraft.entity.item.EntityPainting;
import sun.misc.Unsafe;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Map;

/**
 * 1.12.2 的画作枚举是 final 枚举，无法用 Constructor.newInstance 反射创建；
 * 这里用 Unsafe.allocateInstance 构造 AMULET 画作（16x32，对应上游
 * PaintingVariant(16, 32)），再写入 $VALUES 并清空 Class 的枚举缓存。
 */
@SuppressWarnings("removal")
public final class ModPaintings {

    public static EntityPainting.EnumArt AMULET;

    private ModPaintings() {
    }

    public static void register() {
        if (AMULET != null) return;
        try {
            Unsafe unsafe = unsafe();
            Field valuesField = EntityPainting.EnumArt.class.getDeclaredField("$VALUES");
            Object valuesBase = unsafe.staticFieldBase(valuesField);
            long valuesOffset = unsafe.staticFieldOffset(valuesField);
            valuesField.setAccessible(true);
            EntityPainting.EnumArt[] current =
                    (EntityPainting.EnumArt[]) valuesField.get(null);

            EntityPainting.EnumArt amulet = (EntityPainting.EnumArt)
                    unsafe.allocateInstance(EntityPainting.EnumArt.class);
            putObject(unsafe, amulet, Enum.class, "name", "AMULET");
            putInt(unsafe, amulet, Enum.class, "ordinal", current.length);
            putObject(unsafe, amulet, EntityPainting.EnumArt.class, "title", "Amulet");
            putInt(unsafe, amulet, EntityPainting.EnumArt.class, "sizeX", 16);
            putInt(unsafe, amulet, EntityPainting.EnumArt.class, "sizeY", 32);
            putInt(unsafe, amulet, EntityPainting.EnumArt.class, "offsetX", 0);
            putInt(unsafe, amulet, EntityPainting.EnumArt.class, "offsetY", 0);
            AMULET = amulet;

            EntityPainting.EnumArt[] extended = Arrays.copyOf(current, current.length + 1);
            extended[current.length] = amulet;
            unsafe.putObject(valuesBase, valuesOffset, extended);

            clearEnumCache(unsafe, EntityPainting.EnumArt.class);
        } catch (Exception exception) {
            throw new IllegalStateException(
                    "Unable to register the Wither Storm Amulet painting", exception);
        }
    }

    private static Unsafe unsafe() throws Exception {
        Field field = Unsafe.class.getDeclaredField("theUnsafe");
        field.setAccessible(true);
        return (Unsafe) field.get(null);
    }

    private static void putObject(Unsafe unsafe, Object target, Class<?> owner,
                                  String fieldName, Object value) throws Exception {
        Field field = owner.getDeclaredField(fieldName);
        unsafe.putObject(target, unsafe.objectFieldOffset(field), value);
    }

    private static void putInt(Unsafe unsafe, Object target, Class<?> owner,
                               String fieldName, int value) throws Exception {
        Field field = owner.getDeclaredField(fieldName);
        unsafe.putInt(target, unsafe.objectFieldOffset(field), value);
    }

    private static void clearEnumCache(Unsafe unsafe, Class<?> enumClass) throws Exception {
        clearStatic(unsafe, Class.class, enumClass, "enumConstants");
        clearStatic(unsafe, Class.class, enumClass, "enumConstantDirectory");
    }

    @SuppressWarnings("unchecked")
    private static void clearStatic(Unsafe unsafe, Class<?> fieldOwner, Object target,
                                    String fieldName) throws Exception {
        Field field = fieldOwner.getDeclaredField(fieldName);
        Object value = unsafe.getObject(target, unsafe.objectFieldOffset(field));
        if (value instanceof Map) {
            ((Map<?, ?>) value).clear();
        }
        unsafe.putObject(target, unsafe.objectFieldOffset(field), null);
    }
}
