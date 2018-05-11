package com.github.jccode.slickx.core

import slick.lifted.Rep

trait BaseTable {
  def id: Rep[Int]
}
