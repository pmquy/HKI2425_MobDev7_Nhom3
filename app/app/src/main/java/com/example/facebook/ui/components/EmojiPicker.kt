 package com.example.facebook.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private enum class EmojiCategory(val list: List<String>) {
    SMILEY(
        listOf(
            "😀",
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
            "🙊"
        )
    ),
    PEOPLE(
        listOf(
            "👋",
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
            "🧑‍🧒‍🧒"
        )
    ),
    ANIMALS(
        listOf(
            "🐵",
            "🐒",
            "🦍",
            "🦧",
            "🐶",
            "🐕",
            "🦮",
            "🐕‍🦺",
            "🐩",
            "🐺",
            "🦊",
            "🦝",
            "🐱",
            "🐈",
            "🐈‍⬛",
            "🦁",
            "🐯",
            "🐅",
            "🐆",
            "🐴",
            "🫎",
            "🫏",
            "🐎",
            "🦄",
            "🦓",
            "🦌",
            "🦬",
            "🐮",
            "🐂",
            "🐃",
            "🐄",
            "🐷",
            "🐖",
            "🐗",
            "🐽",
            "🐏",
            "🐑",
            "🐐",
            "🐪",
            "🐫",
            "🦙",
            "🦒",
            "🐘",
            "🦣",
            "🦏",
            "🦛",
            "🐭",
            "🐁",
            "🐀",
            "🐹",
            "🐰",
            "🐇",
            "🐿️",
            "🦫",
            "🦔",
            "🦇",
            "🐻",
            "🐻‍❄️",
            "🐨",
            "🐼",
            "🦥",
            "🦦",
            "🦨",
            "🦘",
            "🦡",
            "🐾",
            "🦃",
            "🐔",
            "🐓",
            "🐣",
            "🐤",
            "🐥",
            "🐦",
            "🐧",
            "🕊️",
            "🦅",
            "🦆",
            "🦢",
            "🦉",
            "🦤",
            "🪶",
            "🦩",
            "🦚",
            "🦜",
            "🪽",
            "🐦‍⬛",
            "🪿",
            "🐦‍🔥",
            "🪹",
            "🪺",
            "🐸",
            "🐊",
            "🐢",
            "🦎",
            "🐍",
            "🐲",
            "🐉",
            "🦕",
            "🦖",
            "🐳",
            "🐋",
            "🐬",
            "🦭",
            "🐟",
            "🐠",
            "🐡",
            "🦈",
            "🐙",
            "🐚",
            "🪸",
            "🪼",
            "🦀",
            "🦞",
            "🦐",
            "🦑",
            "🦪",
            "🐌",
            "🦋",
            "🐛",
            "🐜",
            "🐝",
            "🪲",
            "🐞",
            "🦗",
            "🪳",
            "🕷️",
            "🕸️",
            "🦂",
            "🦟",
            "🪰",
            "🪱",
            "🦠",
            "💐",
            "🌸",
            "💮",
            "🪷",
            "🏵️",
            "🌹",
            "🥀",
            "🌺",
            "🌻",
            "🌼",
            "🌷",
            "🪻",
            "🌱",
            "🪴",
            "🌲",
            "🌳",
            "🌴",
            "🌵",
            "🌾",
            "🌿",
            "☘️",
            "🍀",
            "🍁",
            "🍂",
            "🍃",
            "🍄",
            "🪨",
            "🪵",
            "🌑",
            "🌒",
            "🌓",
            "🌔",
            "🌕",
            "🌖",
            "🌗",
            "🌘",
            "🌙",
            "🌚",
            "🌛",
            "🌜",
            "☀️",
            "🌝",
            "🌞",
            "🪐",
            "⭐",
            "🌟",
            "🌠",
            "🌌",
            "☁️",
            "⛅",
            "⛈️",
            "🌤️",
            "🌥️",
            "🌦️",
            "🌧️",
            "🌨️",
            "🌩️",
            "🌪️",
            "🌫️",
            "🌬️",
            "🌀",
            "🌈",
            "🌂",
            "☂️",
            "☔",
            "⛱️",
            "⚡",
            "❄️",
            "☃️",
            "⛄",
            "☄️",
            "🔥",
            "💧",
            "🌊"
        )
    ),
    FOOD(
        listOf(
            "🍇",
            "🍈",
            "🍉",
            "🍊",
            "🍋",
            "🍋‍🟩",
            "🍌",
            "🍍",
            "🥭",
            "🍎",
            "🍏",
            "🍐",
            "🍑",
            "🍒",
            "🍓",
            "🫐",
            "🥝",
            "🍅",
            "🫒",
            "🥥",
            "🥑",
            "🍆",
            "🥔",
            "🥕",
            "🌽",
            "🌶️",
            "🫑",
            "🥒",
            "🥬",
            "🥦",
            "🧄",
            "🧅",
            "🥜",
            "🫘",
            "🌰",
            "🫚",
            "🫛",
            "🍄‍🟫",
            "🍞",
            "🥐",
            "🥖",
            "🫓",
            "🥨",
            "🥯",
            "🥞",
            "🧇",
            "🧀",
            "🍖",
            "🍗",
            "🥩",
            "🥓",
            "🍔",
            "🍟",
            "🍕",
            "🌭",
            "🥪",
            "🌮",
            "🌯",
            "🫔",
            "🥙",
            "🧆",
            "🥚",
            "🍳",
            "🥘",
            "🍲",
            "🫕",
            "🥣",
            "🥗",
            "🍿",
            "🧈",
            "🧂",
            "🥫",
            "🍝",
            "🍱",
            "🍘",
            "🍙",
            "🍚",
            "🍛",
            "🍜",
            "🍠",
            "🍢",
            "🍣",
            "🍤",
            "🍥",
            "🥮",
            "🍡",
            "🥟",
            "🥠",
            "🥡",
            "🍦",
            "🍧",
            "🍨",
            "🍩",
            "🍪",
            "🎂",
            "🍰",
            "🧁",
            "🥧",
            "🍫",
            "🍬",
            "🍭",
            "🍮",
            "🍯",
            "🍼",
            "🥛",
            "☕",
            "🫖",
            "🍵",
            "🍶",
            "🍾",
            "🍷",
            "🍸",
            "🍹",
            "🍺",
            "🍻",
            "🥂",
            "🥃",
            "🫗",
            "🥤",
            "🧋",
            "🧃",
            "🧉",
            "🥢",
            "🍽️",
            "🍴",
            "🥄",
            "🔪",
            "🫙",
            "🏺"
        )
    ),
    ACTIVITIES(
        listOf(
            "🎃",
            "🎄",
            "🎆",
            "🎇",
            "🧨",
            "✨",
            "🎈",
            "🎉",
            "🎊",
            "🎋",
            "🎍",
            "🎎",
            "🎏",
            "🎐",
            "🎑",
            "🧧",
            "🎁",
            "🎟️",
            "🎫",
            "🏮",
            "🪔",
            "🎖️",
            "🏆",
            "🏅",
            "🥇",
            "🥈",
            "🥉",
            "⚽",
            "⚾",
            "🥎",
            "🏀",
            "🏐",
            "🏈",
            "🏉",
            "🎾",
            "🥏",
            "🎳",
            "🏏",
            "🏑",
            "🏒",
            "🥍",
            "🏓",
            "🏸",
            "🥊",
            "🥋",
            "🥅",
            "⛳",
            "⛸️",
            "🎣",
            "🤿",
            "🎽",
            "🎿",
            "🛷",
            "🥌",
            "🎯",
            "🪀",
            "🪁",
            "🔫",
            "🎱",
            "🔮",
            "🪄",
            "🎮",
            "🕹️",
            "🎰",
            "🎲",
            "🧩",
            "🪅",
            "🪩",
            "🪆",
            "♠️",
            "♥️",
            "♦️",
            "♣️",
            "♟️",
            "🃏",
            "🀄",
            "🎴",
            "🎭",
            "🖼️",
            "🎨"
        )
    ),
}


@Composable
fun EmojiPicker(
    modifier: Modifier = Modifier,
    onClick: (String) -> Unit = {},
) {


    var category by remember { mutableStateOf(EmojiCategory.SMILEY) }

    Column(modifier = modifier) {

        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(EmojiCategory.entries) {
                Card(
                    onClick = { category = it },
                    colors = if (category == it) CardDefaults.cardColors().copy(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    ) else CardDefaults.cardColors().copy(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                        contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                ) {
                    Text(
                        it.name,
                        modifier = Modifier.padding(4.dp),
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
        }

        LazyVerticalGrid(
            columns = GridCells.Adaptive(32.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            items(category.list) {
                Text(
                    it,
                    modifier = Modifier
                        .padding(4.dp)
                        .clickable { onClick(it) },
                    fontSize = 20.sp,
                )
            }
        }
    }

}