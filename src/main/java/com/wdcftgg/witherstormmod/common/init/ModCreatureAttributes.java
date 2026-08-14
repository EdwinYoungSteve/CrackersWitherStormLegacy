package com.wdcftgg.witherstormmod.common.init;

import net.minecraft.entity.EnumCreatureAttribute;
import net.minecraftforge.common.util.EnumHelper;

/** 为 1.12.2 补回上游用于附魔和伤害判定的病化生物类型。 */
public final class ModCreatureAttributes {
    public static final EnumCreatureAttribute SICKENED = createSickenedAttribute();

    private ModCreatureAttributes() {
    }

    public static void bootstrap() {
        // 触发静态初始化，确保实体注册前扩展枚举。
    }

    private static EnumCreatureAttribute createSickenedAttribute() {
        EnumCreatureAttribute attribute = EnumHelper.addCreatureAttribute("SICKENED");
        if (attribute == null) {
            throw new IllegalStateException("Unable to register SICKENED creature attribute");
        }
        return attribute;
    }
}
