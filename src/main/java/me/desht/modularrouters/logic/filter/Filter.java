package me.desht.modularrouters.logic.filter;

import me.desht.modularrouters.item.module.ItemModule;
import me.desht.modularrouters.item.module.Module;
import me.desht.modularrouters.item.smartfilter.ItemSmartFilter;
import me.desht.modularrouters.item.smartfilter.SmartFilter;
import me.desht.modularrouters.logic.ModuleTarget;
import me.desht.modularrouters.logic.filter.matchers.IItemMatcher;
import me.desht.modularrouters.logic.filter.matchers.SimpleItemMatcher;
import me.desht.modularrouters.util.ModuleHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;

import java.util.ArrayList;
import java.util.List;

public class Filter {
    public static final int FILTER_SIZE = 9;
    private final List<IItemMatcher> matchers = new ArrayList<IItemMatcher>();
    private boolean blacklist = true;
    private boolean ignoreMeta = false;
    private boolean ignoreNBT = true;
    private boolean ignoreOredict = true;
    private boolean matchAll = false;

    public Filter() {}

    public Filter(ItemStack moduleStack, ModuleTarget target) {
        if (moduleStack != null && moduleStack.getItem() instanceof ItemModule && moduleStack.hasTagCompound()) {
            NBTTagCompound tag = ModuleHelper.validateNBT(moduleStack);
            blacklist = ModuleHelper.checkFlag(moduleStack, Module.ModuleFlags.BLACKLIST);
            ignoreMeta = ModuleHelper.checkFlag(moduleStack, Module.ModuleFlags.IGNORE_META);
            ignoreNBT = ModuleHelper.checkFlag(moduleStack, Module.ModuleFlags.IGNORE_NBT);
            ignoreOredict = ModuleHelper.checkFlag(moduleStack, Module.ModuleFlags.IGNORE_TAGS);
            matchAll = ModuleHelper.checkFlag(moduleStack, Module.ModuleFlags.MATCH_ALL);
            if (tag.hasKey(ModuleHelper.NBT_FILTER)) {
                NBTTagList list = tag.getTagList(ModuleHelper.NBT_FILTER, 10);
                for (int i = 0; i < list.tagCount(); i++) {
                    NBTTagCompound itemTag = list.getCompoundTagAt(i);
                    ItemStack filterStack = ItemStack.loadItemStackFromNBT(itemTag);
                    if (filterStack != null) {
                        if (filterStack.getItem() instanceof ItemSmartFilter) {
                            SmartFilter f = ItemSmartFilter.getFilter(filterStack);
                            if (f != null) matchers.add(f.compile(filterStack, moduleStack, target));
                        } else {
                            Module module = ItemModule.getModule(moduleStack);
                            if (module != null) matchers.add(module.getFilterItemMatcher(filterStack));
                        }
                    }
                }
            }
        }
    }

    public boolean isMatcherListEmpty() {
        return matchers.isEmpty();
    }

    public boolean allowItem(ItemStack stack) {
        if (matchers.isEmpty()) return true;
        if (matchAll) {
            for (IItemMatcher m : matchers) {
                if (!matchWithFlags(m, stack)) return blacklist;
            }
            return !blacklist;
        }
        for (IItemMatcher m : matchers) {
            if (matchWithFlags(m, stack)) return !blacklist;
        }
        return blacklist;
    }

    public boolean rejectItem(ItemStack stack) {
        return !allowItem(stack);
    }

    private boolean matchWithFlags(IItemMatcher matcher, ItemStack stack) {
        if (matcher instanceof SimpleItemMatcher) {
            ItemStack filterStack = matcher.getStack();
            if (filterStack == null) return false;
            boolean itemMatch = ignoreMeta
                    ? filterStack.getItem() == stack.getItem()
                    : filterStack.isItemEqual(stack);
            if (!itemMatch) return false;
            return ignoreNBT || ItemStack.areItemStackTagsEqual(filterStack, stack);
        }
        return matcher.match(stack);
    }

    

    /**
     * Get raw filter item stacks (for creative module etc.)
     */
    public List<ItemStack> getRawStacks() {
        List<ItemStack> result = new ArrayList<>();
        for (IItemMatcher m : matchers) {
            ItemStack s = m.getStack();
            if (s != null) result.add(s);
        }
        return result;
    }
    public List<IItemMatcher> getMatchers() {
        return matchers;
    }
}
