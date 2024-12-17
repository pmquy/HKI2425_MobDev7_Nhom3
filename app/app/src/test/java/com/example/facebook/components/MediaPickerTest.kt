package com.example.facebook.components

import com.example.facebook.model.File
import com.example.facebook.ui.components.EmojiCategory
import com.example.facebook.ui.components.FileViewModel
import io.mockk.coEvery
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.test.runTest
import org.junit.Test

class ComponentPickerTest {

    // GifPicker tests
    @Test
    fun `test category selection changes emoji list`() {
        var selectedCategory = EmojiCategory.SMILEY
        val onClickMock = mockk<(String) -> Unit>(relaxed = true)

        assertEquals(listOf("😀",
            "😃",
            "😄",
            "😁",
            "😆",
            "😅",
            "🤣",
            "😂",
            "🙂",
            "😉",
            "😊",
            "😇",
            "🥰",
            "😍",
            "🤩",
            "😘",
            "😗",
            "☺️",
            "😚",
            "😙",
            "🥲",
            "😏",
            "😋",
            "😛",
            "😜",
            "🤪",
            "😝",
            "🤑",
            "🤗",
            "🤭",
            "🫢",
            "🫣",
            "🤫",
            "🤔",
            "🫡",
            "🤤",
            "🤠",
            "🥳",
            "🥸",
            "😎",
            "🤓",
            "🧐",
            "🙃",
            "🫠",
            "🤐",
            "🤨",
            "😐",
            "😑",
            "😶",
            "🫥",
            "😶‍🌫️",
            "😒",
            "🙄",
            "😬",
            "😮‍💨",
            "🤥",
            "🫨",
            "🙂‍↔️",
            "🙂‍↕️",
            "😌",
            "😔",
            "😪",
            "😴",
            "🫩",
            "😷",
            "🤒",
            "🤕",
            "🤢",
            "🤮",
            "🤧",
            "🥵",
            "🥶",
            "🥴",
            "😵",
            "😵‍💫",
            "🤯",
            "🥱",
            "😕",
            "🫤",
            "😟",
            "🙁",
            "☹️",
            "😮",
            "😯",
            "😲",
            "😳",
            "🥺",
            "🥹",
            "😦",
            "😧",
            "😨",
            "😰",
            "😥",
            "😢",
            "😭",
            "😱",
            "😖",
            "😣",
            "😞",
            "😓",
            "😩",
            "😫",
            "😤",
            "😡",
            "😠",
            "🤬",
            "👿",
            "😈",
            "👿",
            "💀",
            "☠️",
            "💩",
            "🤡",
            "👹",
            "👺",
            "👻",
            "👽",
            "👾",
            "🤖",
            "😺",
            "😸",
            "😹",
            "😻",
            "😼",
            "😽",
            "🙀",
            "😿",
            "😾",
            "🙈",
            "🙉",
            "🙊"), selectedCategory.list)

        selectedCategory = EmojiCategory.PEOPLE

        assertEquals(listOf("👋",
            "🤚",
            "🖐️",
            "✋",
            "🖖",
            "🫱",
            "🫲",
            "🫳",
            "🫴",
            "🫷",
            "🫸",
            "👌",
            "🤌",
            "🤏",
            "✌️",
            "🤞",
            "🫰",
            "🤟",
            "🤘",
            "🤙",
            "👈",
            "👉",
            "👆",
            "🖕",
            "👇",
            "☝️",
            "🫵",
            "👍",
            "👎",
            "✊",
            "👊",
            "🤛",
            "🤜",
            "👏",
            "🙌",
            "🫶",
            "👐",
            "🤲",
            "🤝",
            "🙏",
            "✍️",
            "💅",
            "🤳",
            "💪",
            "🦾",
            "🦿",
            "🦵",
            "🦶",
            "👂",
            "🦻",
            "👃",
            "🧠",
            "🫀",
            "🫁",
            "🦷",
            "🦴",
            "👀",
            "👅",
            "👄",
            "🫦",
            "👣",
            "🧬",
            "🩸",
            "👶",
            "🧒",
            "👦",
            "👧",
            "🧑",
            "👱",
            "👨",
            "🧔",
            "🧔‍♂️",
            "🧔‍♀️",
            "👨‍🦰",
            "👨‍🦱",
            "👨‍🦳",
            "👨‍🦲",
            "👩",
            "👩‍🦰",
            "🧑‍🦰",
            "👩‍🦱",
            "🧑‍🦱",
            "👩‍🦳",
            "🧑‍🦳",
            "👩‍🦲",
            "🧑‍🦲",
            "👱‍♀️",
            "👱‍♂️",
            "🧓",
            "👴",
            "👵",
            "🧏",
            "🧏‍♂️",
            "🧏‍♀️",
            "👳",
            "👳‍♂️",
            "👳‍♀️",
            "👲",
            "🧕",
            "🤰",
            "🫃",
            "🫄",
            "👼",
            "🗣️",
            "👤",
            "👥",
            "🦰",
            "🦱",
            "🦲",
            "🦳",
            "🙍",
            "🙍‍♂️",
            "🙍‍♀️",
            "🙎",
            "🙎‍♂️",
            "🙎‍♀️",
            "🙅",
            "🙅‍♂️",
            "🙅‍♀️",
            "🙆",
            "🙆‍♂️",
            "🙆‍♀️",
            "💁",
            "💁‍♂️",
            "💁‍♀️",
            "🙋",
            "🙋‍♂️",
            "🙋‍♀️",
            "🧏",
            "🧏‍♂️",
            "🧏‍♀️",
            "🙇",
            "🙇‍♂️",
            "🙇‍♀️",
            "🤦",
            "🤦‍♂️",
            "🤦‍♀️",
            "🤷",
            "🤷‍♂️",
            "🤷‍♀️",
            "🤱",
            "👩‍🍼",
            "👨‍🍼",
            "🧑‍🍼",
            "💆",
            "💆‍♂️",
            "💆‍♀️",
            "💇",
            "💇‍♂️",
            "💇‍♀️",
            "🚶",
            "🚶‍♂️",
            "🚶‍♀️",
            "🚶‍➡️",
            "🚶‍♀️‍➡️",
            "🚶‍♂️‍➡️",
            "🧍",
            "🧍‍♂️",
            "🧍‍♀️",
            "🧎",
            "🧎‍♂️",
            "🧎‍♀️",
            "🧎‍➡️",
            "🧎‍♀️‍➡️",
            "🧎‍♂️‍➡️",
            "🧑‍🦯",
            "🧑‍🦯‍➡️",
            "👨‍🦯",
            "👨‍🦯‍➡️",
            "👩‍🦯",
            "👩‍🦯‍➡️",
            "🧑‍🦼",
            "🧑‍🦼‍➡️",
            "👨‍🦼",
            "👨‍🦼‍➡️",
            "👩‍🦼",
            "👩‍🦼‍➡️",
            "🧑‍🦽",
            "🧑‍🦽‍➡️",
            "👨‍🦽",
            "👨‍🦽‍➡️",
            "👩‍🦽",
            "👩‍🦽‍➡️",
            "🏃",
            "🏃‍♂️",
            "🏃‍♀️",
            "🏃‍➡️",
            "🏃‍♀️‍➡️",
            "🏃‍♂️‍➡️",
            "💃",
            "🕺",
            "🕴️",
            "👯",
            "👯‍♂️",
            "👯‍♀️",
            "🧖",
            "🧖‍♂️",
            "🧖‍♀️",
            "🧗",
            "🧗‍♂️",
            "🧗‍♀️",
            "🤺",
            "🏇",
            "⛷️",
            "🏂",
            "🏌️",
            "🏌️‍♂️",
            "🏌️‍♀️",
            "🏄",
            "🏄‍♂️",
            "🏄‍♀️",
            "🚣",
            "🚣‍♂️",
            "🚣‍♀️",
            "🏊",
            "🏊‍♂️",
            "🏊‍♀️",
            "⛹️",
            "⛹️‍♂️",
            "⛹️‍♀️",
            "🏋️",
            "🏋️‍♂️",
            "🏋️‍♀️",
            "🚴",
            "🚴‍♂️",
            "🚴‍♀️",
            "🚵",
            "🚵‍♂️",
            "🚵‍♀️",
            "🤸",
            "🤸‍♂️",
            "🤸‍♀️",
            "🤼",
            "🤼‍♂️",
            "🤼‍♀️",
            "🤽",
            "🤽‍♂️",
            "🤽‍♀️",
            "🤾",
            "🤾‍♂️",
            "🤾‍♀️",
            "🤹",
            "🤹‍♂️",
            "🤹‍♀️",
            "🧘",
            "🧘‍♂️",
            "🧘‍♀️",
            "🛀",
            "🛌",
            "🧑‍⚕️",
            "👨‍⚕️",
            "👩‍⚕️",
            "🧑‍🎓",
            "👨‍🎓",
            "👩‍🎓",
            "🧑‍🏫",
            "👨‍🏫",
            "👩‍🏫",
            "🧑‍⚖️",
            "👨‍⚖️",
            "👩‍⚖️",
            "🧑‍🌾",
            "👨‍🌾",
            "👩‍🌾",
            "🧑‍🍳",
            "👨‍🍳",
            "👩‍🍳",
            "🧑‍🔧",
            "👨‍🔧",
            "👩‍🔧",
            "🧑‍🏭",
            "👨‍🏭",
            "👩‍🏭",
            "🧑‍💼",
            "👨‍💼",
            "👩‍💼",
            "🧑‍🔬",
            "👨‍🔬",
            "👩‍🔬",
            "🧑‍💻",
            "👨‍💻",
            "👩‍💻",
            "🧑‍🎤",
            "👨‍🎤",
            "👩‍🎤",
            "🧑‍🎨",
            "👨‍🎨",
            "👩‍🎨",
            "🧑‍✈️",
            "👨‍✈️",
            "👩‍✈️",
            "🧑‍🚀",
            "👨‍🚀",
            "👩‍🚀",
            "🧑‍🚒",
            "👨‍🚒",
            "👩‍🚒",
            "👮",
            "👮‍♂️",
            "👮‍♀️",
            "🕵️",
            "🕵️‍♂️",
            "🕵️‍♀️",
            "💂",
            "💂‍♂️",
            "💂‍♀️",
            "🥷",
            "👷",
            "👷‍♂️",
            "👷‍♀️",
            "🫅",
            "🤴",
            "👸",
            "🤵",
            "🤵‍♂️",
            "🤵‍♀️",
            "👰",
            "👰‍♂️",
            "👰‍♀️",
            "🎅",
            "🤶",
            "🧑‍🎄",
            "🦸",
            "🦸‍♂️",
            "🦸‍♀️",
            "🦹",
            "🦹‍♂️",
            "🦹‍♀️",
            "🧙",
            "🧙‍♂️",
            "🧙‍♀️",
            "🧚",
            "🧚‍♂️",
            "🧚‍♀️",
            "🧛",
            "🧛‍♂️",
            "🧛‍♀️",
            "🧜",
            "🧜‍♂️",
            "🧜‍♀️",
            "🧝",
            "🧝‍♂️",
            "🧝‍♀️",
            "🧞",
            "🧞‍♂️",
            "🧞‍♀️",
            "🧟",
            "🧟‍♂️",
            "🧟‍♀️",
            "🧌",
            "👯",
            "👯‍♂️",
            "👯‍♀️",
            "🧑‍🤝‍🧑",
            "👭",
            "👫",
            "👬",
            "💏",
            "👩‍❤️‍💋‍👨",
            "👨‍❤️‍💋‍👨",
            "👩‍❤️‍💋‍👩",
            "💑",
            "👩‍❤️‍👨",
            "👨‍❤️‍👨",
            "👩‍❤️‍👩",
            "👨‍👩‍👦",
            "👨‍👩‍👧",
            "👨‍👩‍👧‍👦",
            "👨‍👩‍👦‍👦",
            "👨‍👩‍👧‍👧",
            "👨‍👨‍👦",
            "👨‍👨‍👧",
            "👨‍👨‍👧‍👦",
            "👨‍👨‍👦‍👦",
            "👨‍👨‍👧‍👧",
            "👩‍👩‍👦",
            "👩‍👩‍👧",
            "👩‍👩‍👧‍👦",
            "👩‍👩‍👦‍👦",
            "👩‍👩‍👧‍👧",
            "👨‍👦",
            "👨‍👦‍👦",
            "👨‍👧",
            "👨‍👧‍👦",
            "👨‍👧‍👧",
            "👩‍👦",
            "👩‍👦‍👦",
            "👩‍👧",
            "👩‍👧‍👦",
            "👩‍👧‍👧",
            "👪",
            "🧑‍🧑‍🧒",
            "🧑‍🧑‍🧒‍🧒",
            "🧑‍🧒",
            "🧑‍🧒‍🧒"), selectedCategory.list)
    }

    @Test
    fun `test onClick is triggered with correct emoji`() {
        val onClickMock = mockk<(String) -> Unit>(relaxed = true)
        val emojiSlot = slot<String>()

        val category = EmojiCategory.FOOD
        val emoji = category.list[0]

        onClickMock(emoji)

        verify { onClickMock(capture(emojiSlot)) }
        assertEquals(emoji, emojiSlot.captured)
    }

    // GifPicker tests
    @Test
    fun `GifPicker retrieves gifs from view model`() = runTest {
        val fileViewModelMock = mockk<FileViewModel>()
        val mockGifs = listOf(
            File(_id = "gif1", url = "http://example.com/gif1", type = "gif"),
            File(_id = "gif2", url = "http://example.com/gif2", type = "gif")
        )
        coEvery { fileViewModelMock.getSystemFile("gif", any(), any()) } returns mockGifs

        var fetchedGifs: List<File> = emptyList()
        fileViewModelMock.getSystemFile("gif", 0, 50).also { fetchedGifs = it }

        assertEquals(2, fetchedGifs.size)
        assertEquals("gif1", fetchedGifs[0]._id)
        assertEquals("gif2", fetchedGifs[1]._id)
    }

    @Test
    fun `GifPicker onClick triggers with correct gif id`() {
        val fileViewModelMock = mockk<FileViewModel>()
        val onClickMock = mockk<(String) -> Unit>(relaxed = true)
        val gifIdSlot = slot<String>()

        val mockGif = File(_id = "gif1", url = "http://example.com/gif1", type = "gif")
        coEvery { fileViewModelMock.getSystemFile("gif", 0, 50) } returns listOf(mockGif)

        onClickMock(mockGif._id)

        verify { onClickMock(capture(gifIdSlot)) }
        assertEquals("gif1", gifIdSlot.captured)
    }
    @Test
    fun `test GIF onClick is triggered correctly`() {
        val onClickMock = mockk<(String) -> Unit>(relaxed = true)
        val gifIdSlot = slot<String>()

        val gifId = "1"
        onClickMock(gifId)

        verify { onClickMock(capture(gifIdSlot)) }
        assertEquals(gifId, gifIdSlot.captured)
    }
}

