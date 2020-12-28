// TODO: 2020-12-29 wand 꾸미기, 파티클 입히기 
package proj.science.bucket1572.mathematics.geometry.orthogonalprojection

import com.github.noonmaru.psychics.AbilityConcept
import com.github.noonmaru.psychics.AbilityType
import com.github.noonmaru.psychics.ActiveAbility
import com.github.noonmaru.psychics.attribute.EsperAttribute
import com.github.noonmaru.psychics.attribute.EsperStatistic
import com.github.noonmaru.psychics.damage.Damage
import com.github.noonmaru.psychics.damage.DamageType
import com.github.noonmaru.psychics.item.isPsychicbound
import com.github.noonmaru.tap.config.Config
import com.github.noonmaru.tap.config.Name
import com.github.noonmaru.tap.fake.setLocation
import net.md_5.bungee.api.ChatColor
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.Particle
import org.bukkit.enchantments.Enchantment
import org.bukkit.event.player.PlayerEvent
import org.bukkit.inventory.ItemStack

@Name("concept")
class OrthoProjConcept : AbilityConcept() {
    /*
        정사영 메타데이터
        분류 : 액티브
        이름 : 정사영
        레벨 제한 :
        쿨타임 :
        마나 소모량 :
        시전 시간 :
        시전 반경 : 20칸
     */

    @Config
    var minFallDistance: Int = 5 // 피해를 입기 위한 최소 거리

    @Config
    var damageByFalling: Double = 0.5 // 낙하로 인한 피해량

    @Config
    var particleCount: Int = 20 // 입자 개수

    init {
        type = AbilityType.ACTIVE
        displayName = "정사영"
        levelRequirement = 0
        cooldownTicks = 0
        cost = 0.0
        castingTicks = 0
        range = 20.0
        val sunLight = ItemStack(Material.SUNFLOWER, 1)
        sunLight.apply {
            val meta = itemMeta
            meta.setDisplayName("태양 광선")
            meta.addEnchant(Enchantment.CHANNELING, 1, false)
            meta.lore = listOf(
                "태양 광선은 지구에 평행하게 입사합니다."
            )
            meta.isPsychicbound = true
            itemMeta = meta
        }
        wand = sunLight
        supplyItems = listOf(
            sunLight
        )
        description = listOf(
            "자신보다 높이 있는 반경 $range 칸 안의 엔티티를",
            "${castingTicks / 20.0}초 이후",
            "지면으로 떨어뜨립니다. 이때 엔티티의 낙하 거리가 $minFallDistance 보다 크다면",
            "해당 엔티티는 추가 낙하 거리 당 $damageByFalling 의 낙하 피해를 입습니다.",
            "",
            "${ChatColor.ITALIC}정사영은 수선의 발의 집합"
        )
    }
}

class OrthogonalProjection : ActiveAbility<OrthoProjConcept>() {
    init {
        targeter = {
            esper.player.location
        }
    }

    override fun onEnable() {
        for (supply in concept.supplyItems) {
            esper.player.inventory.addItem(supply)
        }
    }

    override fun onCast(event: PlayerEvent, action: WandAction, target: Any?) {
        val origin = esper.player.location
        val neighborhood = origin.getNearbyLivingEntities(concept.range)
        neighborhood.remove(esper.player)
        for (neighbor in neighborhood) {
            esper.player.world.spawnParticle(Particle.CLOUD, neighbor.location, concept.particleCount) // 파티클

            var fallDistance = 0 // 낙하 거리
            var coords = Location(
                esper.player.world,
                neighbor.location.x, neighbor.location.y - fallDistance, neighbor.location.z
            ) // 낙하시킬 위치
            while (coords.block.isEmpty) {
                fallDistance += 1
                coords = Location(
                    esper.player.world, neighbor.location.x,
                    neighbor.location.y - fallDistance, neighbor.location.z
                )
            } // 가장 가까운 지면 찾기

            neighbor.setLocation(coords)
            if (fallDistance >= concept.minFallDistance) {
                val damage = Damage(
                    DamageType.RANGED,
                    EsperStatistic.of(
                        EsperAttribute.ATTACK_DAMAGE to
                                concept.damageByFalling * (fallDistance - concept.minFallDistance)
                    )
                )
                neighbor.psychicDamage(damage, esper.player.location, 0.0) // 낙하 피해 (원거리)

                esper.player.world.spawnParticle(Particle.CLOUD, neighbor.location, concept.particleCount) // 파티클
            }
        }
    }

}