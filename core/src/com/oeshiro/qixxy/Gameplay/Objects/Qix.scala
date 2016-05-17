package com.oeshiro.qixxy.Gameplay.Objects

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.g2d.{Sprite, SpriteBatch, TextureRegion}
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.graphics.{Color, Texture}
import com.badlogic.gdx.math.{Circle, Vector2}
import com.badlogic.gdx.utils.Disposable

class Qix(field: GameField)
  extends GameFieldObject(field) with Disposable {

  val LOG = classOf[Qix].getSimpleName

  var texture: Texture = _
  var s_texture: Sprite = _
  val pixelSize = 50

  sealed abstract class QIX_STATE
  case object SLEEPING extends QIX_STATE
  case object MOVING extends QIX_STATE

  var state: QIX_STATE = _
  var nextPoint: Vector2 = _
  override val size: Float = super.size * 4f
  val speed = 100

  val bounds = new Circle(position, size)
  override def getBounds: Circle = {
    bounds.setPosition(position)
    bounds
  }

  // start near the middle of the field
  val startingPosition = new Vector2(
    (field.areaVertices.get(2).x + field.areaVertices.get(3).x) * 0.5f,
    (field.areaVertices.get(2).y + field.areaVertices.get(1).y) * 0.5f)

  init()

  def init() {
    terminalVelocity.set(speed, speed)
    state = SLEEPING
    nextPoint = new Vector2()

    initSprites()

    position.set(startingPosition)
    velocity.set(terminalVelocity)
  }

  def initSprites() {
    texture = new Texture(Gdx.files.internal("raw/qix.png"))
    s_texture = new Sprite(new TextureRegion(texture))
    s_texture.setScale(0.1f)
  }

  def render(batch: SpriteBatch, shaper: ShapeRenderer) {
    drawQix(shaper, batch)
    // TODO Delete
    shaper.circle(nextPoint.x, nextPoint.y, 1)
  }

  private def drawQix(shaper: ShapeRenderer, batch: SpriteBatch) {
    shaper.setColor(Color.BLUE)
    shaper.circle(position.x, position.y, size)
    shaper.setColor(Color.WHITE)
    s_texture.draw(batch)
  }

  private def rotate(newPos: Vector2) {
    s_texture.setRotation(newPos.angle())
  }

  private def checkCollisionWithBorders(pos: Vector2): Boolean =
    isOnAreaBorder(field.areaVertices, pos, size) || !isInArea(field.area, pos)

  override def update(delta: Float) {
    state match {
      case SLEEPING =>
        while (!isInArea(field.area, nextPoint)
               || isOnAreaBorder(field.areaVertices, nextPoint, size)
               || position.epsilonEquals(nextPoint, size * 0.5f))
          nextPoint.set(field.getRandomPoint)
        state = MOVING

      case MOVING =>
        val vel = nextPoint.cpy()
          .sub(position)
          .nor()
          .scl(velocity.len() * delta)
        val newPos = position.cpy().add(vel)
        rotate(vel)
        if (checkCollisionWithBorders(newPos)) {
          state = SLEEPING
          nextPoint.set(position)
          return
        }
        if (vel.len2() >= nextPoint.cpy().sub(position).len2()) {
          newPos.set(nextPoint.cpy())
          state = SLEEPING
        }
        position.set(newPos)
    }
    s_texture.setPosition(position.x - pixelSize * 4.8f, position.y - pixelSize * 4.8f)
  }

  override def dispose() {
    texture.dispose()
  }
}