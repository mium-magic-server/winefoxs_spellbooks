package net.magicterra.winefoxsspellbooks.compat.jade;

import net.magicterra.winefoxsspellbooks.entity.spells.SummonedEntityMaid;
import snownee.jade.api.IWailaClientRegistration;
import snownee.jade.api.IWailaPlugin;
import snownee.jade.api.WailaPlugin;

/**
 * Jade 兼容
 *
 * @author Gardel &lt;gardel741@outlook.com&gt;
 * @since 2026-01-23 01:14
 */
@WailaPlugin
public class JadePlugin implements IWailaPlugin {
    @Override
    public void registerClient(IWailaClientRegistration registration) {
        registration.registerEntityComponent(SummonedMaidProvider.INSTANCE, SummonedEntityMaid.class);
    }
}
