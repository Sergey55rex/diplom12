package ru.kot1.demo.entity.forWorker

import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import ru.kot1.demo.dto.Attachment
import ru.kot1.demo.dto.Coords
import ru.kot1.demo.dto.Post

@Entity
data class PostWorkEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long,
    val author: String?,
    val authorAvatar: String?,
    val authorId: Int,
    val content: String?,
    @Embedded
    val coords: Coords?,
    val likeOwnerIds: List<String>?,
    val likedByMe: Boolean,
    val link: String?,
    val mentionIds: List<String>?,
    val mentionedMe: Boolean,
    val published: String?,
    @Embedded
    val attachment: Attachment?,
    val mediaUri: String?,
    val mediaType: String?,
    val downloadingProgress : Byte?
) {
    fun toDto() = Post(attachment, author, authorAvatar,authorId, content, coords, id,
        likeOwnerIds, likedByMe, link, mentionIds, mentionedMe, published,downloadingProgress)

    companion object {
        fun fromDto(dto: Post) =
            PostWorkEntity(dto.id,dto.author, dto.authorAvatar,dto.authorId,dto.content, dto.coords,
                dto.likeOwnerIds,dto.likedByMe,dto.link,dto.mentionIds, dto.mentionedMe,
                dto.published, dto.attachment, "", "", dto.downloadingProgress)
    }
}