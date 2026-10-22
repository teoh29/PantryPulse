package repository

import traits.Identifiable

// ai-assisted: #2
// why: AI assistance was used to draft a generic Repository trait for entity lookup.
trait Repository[T <: Identifiable]:
  def all: Vector[T]

  def add(record: T): Vector[T]
