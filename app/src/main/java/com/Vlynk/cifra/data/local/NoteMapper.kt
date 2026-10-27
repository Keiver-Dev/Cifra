package com.Vlynk.cifra.data.local

import com.Vlynk.cifra.domain.model.Note

/**
 * Maps a Room [NoteEntity] to a domain [Note].
 */
fun NoteEntity.toDomain(): Note = Note(
    id = id,
    title = title,
    content = content,
    createdAt = createdAt,
    updatedAt = updatedAt,
    category = category
)

/**
 * Maps a domain [Note] to a Room [NoteEntity].
 */
fun Note.toEntity(): NoteEntity = NoteEntity(
    id = id,
    title = title,
    content = content,
    createdAt = createdAt,
    updatedAt = updatedAt,
    category = category
)
