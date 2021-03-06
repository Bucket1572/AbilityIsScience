package proj.science.bucket1572.mathematics.geometry.congruence

import com.github.noonmaru.psychics.AbilityConcept
import com.github.noonmaru.psychics.AbilityType
import com.github.noonmaru.psychics.ActiveAbility
import com.github.noonmaru.psychics.item.isPsychicbound
import com.github.noonmaru.tap.config.Config
import com.github.noonmaru.tap.config.Name
import net.md_5.bungee.api.ChatColor
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.enchantments.Enchantment
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.player.PlayerEvent
import org.bukkit.inventory.ItemStack

@Name("concept")
class CongruenceConcept : AbilityConcept() {

    @Config
    var addedCost: Double = 0.0 // 가격 증가

    init {
        type = AbilityType.ACTIVE
        displayName = "합동"
        levelRequirement = 0
        cooldownTicks = 0
        cost = 0.0
        castingTicks = 0
        val star = ItemStack(Material.NETHER_STAR, 1)
        star.apply {
            val meta = itemMeta
            meta.setDisplayName("${ChatColor.GOLD}${ChatColor.BOLD}합동")
            meta.addEnchant(Enchantment.SILK_TOUCH, 1, false)
            meta.lore = listOf(
                "한 능력을 연속 두 번 쓸 수 있는 기하의 특수기"
            )
            meta.isPsychicbound = true
            itemMeta = meta
        }
        wand = star
        supplyItems = listOf(
            star
        )
        description = listOf(
            "자신의 능력 중 하나를 복제할 수 있습니다.",
            "복사 된 능력은 ${addedCost}만큼 비용이 증가합니다.",
            "${ChatColor.RED}${ChatColor.BOLD}단, 패시브 능력은 복사할 수 없습니다.",
            "",
            "${ChatColor.ITALIC}삼각형의 합동 조건 : SSS, ASA, SAS"
        )
    }
}

class Congruence : ActiveAbility<CongruenceConcept>(), Listener {
    var menu = Bukkit.createInventory(null, 9, "abilityMenu")
    var copiedAbility: ActiveAbility<*>? = null

    init {
        targeter = {
            esper.player.location
        }
    }

    override fun onEnable() {
        psychic.registerEvents(this)
    }

    override fun onCast(event: PlayerEvent, action: WandAction, target: Any?) {
        if (copiedAbility == null) {
            val abilities = esper.psychic!!.abilities
            val centerCount: Int = abilities.size
            val nullItem = ItemStack(Material.BLACK_STAINED_GLASS_PANE, 1)
            nullItem.apply {
                val meta = itemMeta
                meta.setDisplayName(" ")
                itemMeta = meta
            }
            for (i in 0..8) {
                if (i < 4 - centerCount / 2) {
                    menu.setItem(i, nullItem)
                } else if (i <= 4 + centerCount / 2) {
                    if (centerCount % 2 == 0) {
                        if (i < 4) {
                            val idx: Int = i - (4 - centerCount / 2)
                            menu.setItem(i, abilities.get(idx).concept.wand)
                        } else if (i > 4) {
                            val idx: Int = i - (4 - centerCount / 2) - 1
                            menu.setItem(i, abilities.get(idx).concept.wand)
                        } else {
                            menu.setItem(i, nullItem)
                        }
                    } else {
                        val idx: Int = i - (4 - centerCount / 2)
                        menu.setItem(i, abilities.get(idx).concept.wand)
                    }
                } else {
                    menu.setItem(i, nullItem)
                }
            } // 능력 고르는 메뉴 보이기

            esper.player.openInventory(menu)
            return
        }

        esper.psychic!!.mana -= concept.addedCost
        copiedAbility!!.onCast(event, action, target)
    }

    @EventHandler
    fun onInventoryClick(event: InventoryClickEvent) {
        if (event.inventory != menu) return

        event.isCancelled = true

        val item: ItemStack = event.currentItem ?: return

        if ((item.type != Material.BLACK_STAINED_GLASS_PANE) and (item != concept.wand)) {
            val copyAbility = esper.psychic!!.getAbilityByWand(item)!!

            if (copyAbility.concept.type == AbilityType.ACTIVE) {
                copiedAbility = copyAbility as ActiveAbility<*>
                this.targeter = copiedAbility!!.targeter
                esper.player.sendActionBar("${ChatColor.GREEN}${copiedAbility!!.concept.displayName}을 복제했습니다.")
                event.whoClicked.closeInventory()
            } else return
        }
    }
}