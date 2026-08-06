package com.wdcftgg.witherstormmod.client.model.witherstorm;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelBox;
import net.minecraft.client.model.ModelRenderer;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class ModelBuilders {
    private ModelBuilders() { }

    public static final class CubeDeformation {
        public static final CubeDeformation f_171458_ = new CubeDeformation(0.0F);
        final float inflate;
        public CubeDeformation(float inflate) { this.inflate = inflate; }
        public CubeDeformation m_171469_(float amount) { return new CubeDeformation(inflate + amount); }
    }

    public static final class PartPose {
        public static final PartPose f_171404_ = new PartPose(0, 0, 0, 0, 0, 0);
        final float x, y, z, xRot, yRot, zRot;
        private PartPose(float x, float y, float z, float xRot, float yRot, float zRot) {
            this.x = x; this.y = y; this.z = z; this.xRot = xRot; this.yRot = yRot; this.zRot = zRot;
        }
        public static PartPose m_171419_(float x, float y, float z) { return new PartPose(x, y, z, 0, 0, 0); }
        public static PartPose m_171423_(float x, float y, float z, float xRot, float yRot, float zRot) {
            return new PartPose(x, y, z, xRot, yRot, zRot);
        }
    }

    private static final class Cube {
        final int u, v;
        final float x, y, z, width, height, depth, inflate, textureScaleU, textureScaleV;
        final boolean mirror;
        Cube(int u, int v, float x, float y, float z, float width, float height, float depth, float inflate,
             float textureScaleU, float textureScaleV, boolean mirror) {
            this.u = u; this.v = v; this.x = x; this.y = y; this.z = z;
            this.width = width; this.height = height; this.depth = depth; this.inflate = inflate;
            this.textureScaleU = textureScaleU;
            this.textureScaleV = textureScaleV;
            this.mirror = mirror;
        }
    }

    public static final class CubeListBuilder {
        private final List<Cube> cubes = new ArrayList<Cube>();
        private int textureU;
        private int textureV;
        private boolean mirror;
        public static CubeListBuilder m_171558_() { return new CubeListBuilder(); }
        public CubeListBuilder m_171514_(int u, int v) { textureU = u; textureV = v; return this; }
        public CubeListBuilder m_171480_() { return m_171555_(true); }
        public CubeListBuilder m_171555_(boolean mirror) { this.mirror = mirror; return this; }
        public CubeListBuilder m_171488_(float x, float y, float z, float width, float height, float depth, CubeDeformation deformation) {
            cubes.add(new Cube(textureU, textureV, x, y, z, width, height, depth, deformation.inflate, 1.0F, 1.0F, mirror));
            return this;
        }
        public CubeListBuilder m_171506_(float x, float y, float z, float width, float height, float depth, boolean mirror) {
            cubes.add(new Cube(textureU, textureV, x, y, z, width, height, depth, 0.0F, 1.0F, 1.0F, mirror));
            return this;
        }
        public CubeListBuilder m_171496_(float x, float y, float z, float width, float height, float depth,
                                         CubeDeformation deformation, float textureScaleU, float textureScaleV) {
            cubes.add(new Cube(textureU, textureV, x, y, z, width, height, depth, deformation.inflate,
                    textureScaleU, textureScaleV, mirror));
            return this;
        }
    }

    public static final class PartDefinition {
        private final ModelBase model;
        private final ModelRenderer renderer;
        private final Map<String, PartDefinition> children = new LinkedHashMap<String, PartDefinition>();

        public PartDefinition(ModelBase model, ModelRenderer renderer) {
            this.model = model;
            this.renderer = renderer;
        }

        public PartDefinition m_171599_(String name, CubeListBuilder builder, PartPose pose) {
            ModelRenderer child = new ModelRenderer(model);
            child.setRotationPoint(pose.x, pose.y, pose.z);
            child.rotateAngleX = pose.xRot;
            child.rotateAngleY = pose.yRot;
            child.rotateAngleZ = pose.zRot;
            for (Cube cube : builder.cubes) {
                child.setTextureOffset(cube.u, cube.v);
                child.textureWidth = model.textureWidth * cube.textureScaleU;
                child.textureHeight = model.textureHeight * cube.textureScaleV;
                child.cubeList.add(new ModelBox(child, cube.u, cube.v, cube.x, cube.y, cube.z,
                        Math.round(cube.width), Math.round(cube.height), Math.round(cube.depth), cube.inflate, cube.mirror));
            }
            child.textureWidth = model.textureWidth;
            child.textureHeight = model.textureHeight;
            renderer.addChild(child);
            PartDefinition definition = new PartDefinition(model, child);
            children.put(name, definition);
            return definition;
        }

        public PartDefinition child(String name) { return children.get(name); }
        public PartDefinition m_171597_(String name) { return children.get(name); }
        public Map<String, PartDefinition> children() { return children; }
        public ModelRenderer renderer() { return renderer; }
    }
}
