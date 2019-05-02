package com.yogpc.qp.machines.item

import com.yogpc.qp.machines.quarry.TileBasic
import net.minecraft.entity.player.{EntityPlayer, InventoryPlayer}
import net.minecraft.util.INameable
import net.minecraft.world.IInteractionObject

abstract class YSetterInteractionObject(nameable: INameable) extends IInteractionObject {

  override def getGuiID = ItemYSetter.GUI_ID

  override def getName = nameable.getName

  override def hasCustomName = nameable.hasCustomName

  override def getCustomName = nameable.getCustomName
}

object YSetterInteractionObject {

  import GuiQuarryLevel._

  def apply(tile: INameable): IInteractionObject = tile match {
    case basic: TileBasic => new Basic(basic)
    case _ => new YSetterInteractionObject(tile) {
      override def createContainer(playerInventory: InventoryPlayer, playerIn: EntityPlayer) = null
    }
  }

  private class Basic(basic: TileBasic) extends YSetterInteractionObject(basic) {
    override def createContainer(playerInventory: InventoryPlayer, playerIn: EntityPlayer) = new ContainerQuarryLevel(basic, playerIn)
  }

}