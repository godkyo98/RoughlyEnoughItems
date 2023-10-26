/*
 * This file is licensed under the MIT License, part of Roughly Enough Items.
 * Copyright (c) 2018, 2019, 2020, 2021, 2022, 2023 shedaniel
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package me.shedaniel.rei.impl.client.gui.config.components;

import com.google.common.base.MoreObjects;
import com.mojang.math.Matrix4f;
import me.shedaniel.math.Point;
import me.shedaniel.math.Rectangle;
import me.shedaniel.math.impl.PointHelper;
import me.shedaniel.rei.api.client.gui.widgets.Label;
import me.shedaniel.rei.api.client.gui.widgets.WidgetWithBounds;
import me.shedaniel.rei.api.client.gui.widgets.Widgets;
import me.shedaniel.rei.api.client.util.MatrixUtils;
import me.shedaniel.rei.api.common.util.CollectionUtils;
import me.shedaniel.rei.impl.client.gui.config.REIConfigScreen;
import me.shedaniel.rei.impl.client.gui.config.options.CompositeOption;
import me.shedaniel.rei.impl.client.gui.config.options.OptionValueEntry;
import me.shedaniel.rei.impl.client.gui.modules.Menu;
import me.shedaniel.rei.impl.client.gui.modules.entries.ToggleMenuEntry;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.Map;
import java.util.Objects;

import static me.shedaniel.rei.impl.client.gui.config.options.ConfigUtils.literal;
import static me.shedaniel.rei.impl.client.gui.config.options.ConfigUtils.translatable;

public class ConfigOptionValueWidget {
    public static <T> WidgetWithBounds create(CompositeOption<T> option) {
        Map<CompositeOption<?>, ?> defaultOptions = ((REIConfigScreen) Minecraft.getInstance().screen).getDefaultOptions();
        Map<CompositeOption<?>, ?> options = ((REIConfigScreen) Minecraft.getInstance().screen).getOptions();
        OptionValueEntry<T> entry = option.getEntry();
        T value = (T) options.get(option);
        Component[] text = new Component[1];
        
        if (entry instanceof OptionValueEntry.Selection<T> selection) {
            text[0] = selection.getOption(value);
        } else {
            text[0] = literal(value.toString());
        }
        
        if (value.equals(Objects.requireNonNullElseGet(option.getDefaultValue(), () -> (T) defaultOptions.get(option)))) {
            text[0] = translatable("config.rei.value.default", text[0]);
        }
        
        Matrix4f[] matrix = {new Matrix4f()};
        Label label = Widgets.createLabel(new Point(), text[0]).rightAligned()
                .color(0xFFE0E0E0)
                .hoveredColor(0xFFE0E0E0)
                .onRender((poses, l) -> {
                    if (MatrixUtils.transform(matrix[0], l.getBounds()).contains(PointHelper.ofMouse())) {
                        l.setMessage(text[0].copy().withStyle(ChatFormatting.UNDERLINE));
                    } else {
                        l.setMessage(text[0]);
                    }
                });
        
        if (entry instanceof OptionValueEntry.Selection<T> selection) {
            int noOfOptions = selection.getOptions().size();
            if (noOfOptions == 2) {
                label.clickable().onClick($ -> {
                    ((Map<CompositeOption<?>, Object>) options).put(option, selection.getOptions().get((selection.getOptions().indexOf((T) options.get(option)) + 1) % 2));
                    text[0] = selection.getOption((T) options.get(option));
                    
                    if (options.get(option).equals(Objects.requireNonNullElseGet(option.getDefaultValue(), () -> (T) defaultOptions.get(option)))) {
                        text[0] = translatable("config.rei.value.default", text[0]);
                    }
                });
            } else if (noOfOptions >= 2) {
                label.clickable().onClick($ -> {
                    Menu menu = new Menu(MatrixUtils.transform(matrix[0], label.getBounds()), CollectionUtils.map(selection.getOptions(), opt -> {
                        Component selectionOption = selection.getOption(opt);
                        if (opt.equals(defaultOptions.get(option))) {
                            selectionOption = translatable("config.rei.value.default", selectionOption);
                        }
                        
                        return ToggleMenuEntry.of(selectionOption, () -> false, o -> {
                            ((REIConfigScreen) Minecraft.getInstance().screen).closeMenu();
                            ((Map<CompositeOption<?>, Object>) options).put(option, opt);
                            text[0] = selection.getOption(opt);
                            
                            if (options.get(option).equals(defaultOptions.get(option))) {
                                text[0] = translatable("config.rei.value.default", text[0]);
                            }
                        });
                    }), false);
                    ((REIConfigScreen) Minecraft.getInstance().screen).closeMenu();
                    ((REIConfigScreen) Minecraft.getInstance().screen).openMenu(menu);
                });
            }
        }
        
        return Widgets.concatWithBounds(() -> new Rectangle(-label.getBounds().width, 0, label.getBounds().width + 8, 14),
                label,
                Widgets.createDrawableWidget((helper, matrices, mouseX, mouseY, delta) -> matrix[0] = matrices.last().pose()),
                Widgets.withTranslate(Widgets.createTexturedWidget(new ResourceLocation("roughlyenoughitems:textures/gui/config/selector.png"),
                        new Rectangle(1, 1, 4, 6), 0, 0, 1, 1, 1, 1), 0, 0.5, 0)
        );
    }
}
