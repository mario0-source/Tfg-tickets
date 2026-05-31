package com.example.myapplication.ocr

import com.example.myapplication.model.ProductDto

data class ParsedReceipt(
    val nombre: String,
    val precio: String,
    val fecha: String,
    val categoria: String,
    val productos: List<ProductDto>,
    val hasValidProducts: Boolean = productos.isNotEmpty()
)

object ReceiptOcrParser {

    private data class ParsedProduct(val name: String, val price: Double)

    private const val FALLBACK_PRODUCT_NAME = "Compra"

    private val METADATA_LINE_KEYWORDS = setOf(
        "subtotal", "iva", "igic", "impuesto", "base imponible", "cuota", "fecha", "hora",
        "vendedor", "vendedora", "cajero", "cajera", "factura", "simplificada", "recibo",
        "gracias", "visite", "cambio", "efectivo", "tarjeta", "pago", "operacion", "operación",
        "transaccion", "transacción", "autorizacion", "autorización", "terminal", "cliente",
        "nif", "cif", "telefono", "teléfono", "telf", "domicilio", "direccion", "dirección",
        "horario", "apertura", "cierre", "articulos", "artículos", "descuento", "entrega",
        "devolucion", "devolución", "atencion", "atención", "atendio", "atendió",
        "datos fiscales", "regimen", "régimen", "equivalencia", "copia", "unidades", "pvp"
    )

    private val METADATA_NAME_KEYWORDS = setOf(
        "subtotal", "iva", "igic", "impuesto", "fecha", "hora", "vendedor", "vendedora",
        "cajero", "cajera", "factura", "recibo", "cambio", "efectivo", "tarjeta", "nif",
        "cif", "telefono", "teléfono", "domicilio", "direccion", "dirección", "horario",
        "apertura", "cierre", "articulos", "artículos", "descuento", "base", "cuota",
        "cliente", "terminal", "gracias", "visite", "total", "importe", "suma", "ticket"
    )

    private val TOTAL_LABEL_REGEX = Regex(
        """^\s*(?:total|importe|suma|a\s+pagar|pagado|amount\s+due)\b""",
        RegexOption.IGNORE_CASE
    )

    private val TOTAL_INLINE_REGEX = Regex(
        """(?:^|\b)(?:total|importe|suma|a\s+pagar|pagado)\b[^0-9]{0,20}(\d+[.,]\d{1,2})""",
        RegexOption.IGNORE_CASE
    )

    private val LEADING_QTY = Regex("""^(\d+)\s*(?:[xX×]\s*|\s+)""")

    private val TRAILING_PRICE = Regex(
        """(?:€|eur\s*)?(\d+[.,]\d{1,2})\s*(?:€|eur)?$""",
        RegexOption.IGNORE_CASE
    )

    private val DATE_LINE_REGEX = Regex(
        """^(?:fecha|date)?\s*:?\s*(\d{1,2}[/.-]\d{1,2}[/.-]\d{2,4})""",
        RegexOption.IGNORE_CASE
    )

    private val DATE_REGEX = Regex("""(\d{1,2})[/.-](\d{1,2})[/.-](\d{2,4})""")
    private val TIME_REGEX = Regex("""\b([01]?\d|2[0-3]):[0-5]\d(?:\s*[-–]\s*([01]?\d|2[0-3]):[0-5]\d)?\b""")
    private val WWW_REGEX = Regex("""(www\.|https?://)""", RegexOption.IGNORE_CASE)
    private val PERCENT_REGEX = Regex("""\d+\s*%""")
    private val PHONE_REGEX = Regex("""\b(?:\+34\s?)?(?:[6-7]\d{2}|[89]\d{2})\s?\d{2}\s?\d{2}\s?\d{2}\b""")

    private val WEEKDAY_KEYWORDS = setOf(
        "lunes", "martes", "miercoles", "miércoles", "jueves", "viernes", "sabado", "sábado", "domingo"
    )

    private val FOOD_KEYWORDS = setOf(
        "leche", "pan", "arroz", "pasta", "carne", "pollo", "pescado", "fruta", "verdura",
        "yogur", "yogurt", "queso", "huevo", "huevos", "aceite", "azucar", "azúcar", "sal",
        "jamón", "jamon", "embutido", "cereal", "galleta", "chocolate", "mantequilla",
        "tomate", "patata", "cebolla", "plátano", "platano", "manzana", "naranja", "atun", "atún"
    )

    private val DRINK_KEYWORDS = setOf(
        "coca", "pepsi", "fanta", "sprite", "agua", "cerveza", "vino", "zumo", "refresco",
        "cola", "nestea", "aquarius", "monster", "café", "cafe", "batido", "licor"
    )

    private val HOME_KEYWORDS = setOf(
        "detergente", "lejía", "lejia", "suavizante", "papel", "servilleta", "pañuelo",
        "champú", "champu", "gel", "jabón", "jabon", "esponja", "limpiador", "bolsa"
    )

    private val TECH_KEYWORDS = setOf(
        "usb", "cable", "pila", "pilas", "bateria", "batería", "auricular", "cargador", "memoria"
    )

    fun parse(text: String): ParsedReceipt {
        val lines = normalizeLines(text)
        val rawProducts = extractProducts(lines)
        var total = extractTotal(text, lines, rawProducts.map { it.price })
        var products = applySingleProductTotalRule(rawProducts, total)

        if (products.isEmpty() && total == null) {
            total = extractTotalFromBottom(lines, emptyList())
        }

        if (products.isEmpty() && total != null) {
            products = listOf(ParsedProduct(name = FALLBACK_PRODUCT_NAME, price = total))
        }

        val productDtos = products.map {
            ProductDto(nombre = formatProductName(it.name), precio = it.price)
        }

        if (total == null && productDtos.isNotEmpty()) {
            total = productDtos.sumOf { it.precio ?: 0.0 }.takeIf { it > 0 }
        }

        return ParsedReceipt(
            nombre = extractStoreName(lines),
            precio = total?.let { "%.2f".format(it) } ?: "",
            fecha = extractDate(text, lines),
            categoria = inferCategoryFromProducts(productDtos),
            productos = productDtos
        )
    }

    private fun normalizeLines(text: String): List<String> {
        return text.lines()
            .map { it.trim() }
            .filter { it.isNotBlank() }
    }

    private fun extractProducts(lines: List<String>): List<ParsedProduct> {
        val seen = mutableSetOf<String>()

        return lines.mapNotNull { line ->
            if (isMetadataLine(line)) return@mapNotNull null

            val withoutQty = LEADING_QTY.replace(line, "").trim()
            val priceMatch = findTrailingPrice(withoutQty) ?: return@mapNotNull null
            val price = parsePrice(priceMatch) ?: return@mapNotNull null

            val rawName = withoutQty
                .substring(0, priceMatch.range.first)
                .replace(Regex("""\s+"""), " ")
                .trim()
                .trimEnd('-', '.', ':', '*', '€')

            if (!isValidProductLine(rawName, price, line)) return@mapNotNull null

            val key = rawName.lowercase()
            if (key in seen) return@mapNotNull null
            seen.add(key)

            ParsedProduct(name = rawName, price = price)
        }
    }

    private fun findTrailingPrice(line: String): MatchResult? {
        return TRAILING_PRICE.find(line)
            ?: TRAILING_PRICE.find(line.replace(" ", ""))
    }

    private fun parsePrice(match: MatchResult): Double? {
        return parsePriceString(match.groupValues[1])
    }

    private fun parsePriceString(raw: String): Double? {
        return raw.replace(",", ".")
            .trim()
            .toDoubleOrNull()
            ?.takeIf { it in 0.01..9999.99 }
    }

    private fun isMetadataLine(line: String): Boolean {
        val lower = line.lowercase().trim()

        if (lower.length < 2) return true
        if (WWW_REGEX.containsMatchIn(lower)) return true
        if (PERCENT_REGEX.containsMatchIn(line)) return true
        if (PHONE_REGEX.containsMatchIn(line)) return true
        if (DATE_LINE_REGEX.matches(lower)) return true
        if (isTotalLine(lower)) return true
        if (DATE_REGEX.matches(line) && line.length <= 12) return true
        if (TIME_REGEX.containsMatchIn(line) && !looksLikeProductWithPrice(line)) return true
        if (WEEKDAY_KEYWORDS.any { lower.contains(it) }) return true

        if (METADATA_LINE_KEYWORDS.any { keyword -> containsKeyword(lower, keyword) }) {
            return true
        }

        return false
    }

    private fun isTotalLine(lower: String): Boolean {
        if (TOTAL_INLINE_REGEX.containsMatchIn(lower)) return true
        if (TOTAL_LABEL_REGEX.containsMatchIn(lower) && findTrailingPrice(lower) != null) return true
        return lower.trim() in setOf("total", "importe", "suma")
    }

    private fun isValidProductLine(name: String, price: Double, originalLine: String): Boolean {
        if (name.length < 2) return false
        if (name.count { it.isLetter() } < 2) return false
        if (price <= 0.0 || price > 9999.99) return false

        val lowerName = name.lowercase()
        if (TIME_REGEX.containsMatchIn(name)) return false
        if (DATE_REGEX.matches(name)) return false
        if (PERCENT_REGEX.containsMatchIn(name)) return false
        if (WWW_REGEX.containsMatchIn(lowerName)) return false
        if (isTotalLine(lowerName)) return false

        if (METADATA_NAME_KEYWORDS.any { keyword -> containsKeyword(lowerName, keyword) }) {
            return false
        }

        val words = lowerName.split(Regex("""\s+"""))
        if (words.all { it.matches(Regex("""\d+""")) }) return false

        return true
    }

    private fun looksLikeProductWithPrice(line: String): Boolean {
        val match = findTrailingPrice(line) ?: return false
        val name = line.substring(0, match.range.first).replace(Regex("""\s+"""), " ").trim()
        return name.length >= 2 && name.count { it.isLetter() } >= 2
    }

    private fun containsKeyword(text: String, keyword: String): Boolean {
        return text == keyword ||
                text.startsWith("$keyword ") ||
                text.startsWith("$keyword:") ||
                text.endsWith(" $keyword") ||
                Regex("""\b${Regex.escape(keyword)}\b""").containsMatchIn(text)
    }

    private fun applySingleProductTotalRule(
        products: List<ParsedProduct>,
        total: Double?
    ): List<ParsedProduct> {
        if (products.size != 1 || total == null) return products

        val only = products.first()
        if (only.name.equals(FALLBACK_PRODUCT_NAME, ignoreCase = true)) {
            return listOf(only.copy(price = total))
        }

        val priceMatchesTotal = kotlin.math.abs(only.price - total) < 0.05
        return if (priceMatchesTotal) listOf(only) else listOf(only.copy(price = total))
    }

    private fun formatProductName(name: String): String {
        if (name.equals(FALLBACK_PRODUCT_NAME, ignoreCase = true)) return FALLBACK_PRODUCT_NAME

        return name.trim()
            .replace(Regex("""\s+"""), " ")
            .split(" ")
            .joinToString(" ") { word ->
                word.lowercase().replaceFirstChar { char ->
                    if (char.isLowerCase()) char.titlecase() else char.toString()
                }
            }
    }

    private fun extractStoreName(lines: List<String>): String {
        return lines.asSequence()
            .filter { line ->
                val lower = line.lowercase()
                !isMetadataLine(line) &&
                        !looksLikeProductWithPrice(line) &&
                        !DATE_REGEX.matches(line) &&
                        !WWW_REGEX.containsMatchIn(lower) &&
                        line.length in 3..60 &&
                        line.count { it.isLetter() } >= 3
            }
            .firstOrNull()
            ?.trim()
            ?: ""
    }

    private fun extractTotal(text: String, lines: List<String>, productPrices: List<Double>): Double? {
        TOTAL_INLINE_REGEX.findAll(text)
            .mapNotNull { parsePriceString(it.groupValues[1]) }
            .lastOrNull()
            ?.let { return it }

        for (index in lines.indices) {
            val line = lines[index]
            val lower = line.lowercase()

            if (!TOTAL_LABEL_REGEX.containsMatchIn(lower) && lower.trim() !in setOf("total", "importe", "suma")) {
                continue
            }

            findTrailingPrice(line)?.let { return parsePrice(it) }

            if (index + 1 < lines.size) {
                val nextLine = lines[index + 1]
                findTrailingPrice(nextLine)?.let { return parsePrice(it) }
                parsePriceString(nextLine)?.let { return it }
            }
        }

        if (productPrices.isNotEmpty()) {
            val sum = productPrices.sum()
            if (sum > 0) return sum
        }

        extractTotalFromBottom(lines, productPrices)?.let { return it }

        return productPrices.maxOrNull()
    }

    private fun extractTotalFromBottom(lines: List<String>, productPrices: List<Double>): Double? {
        val productPriceSet = productPrices.toSet()

        val candidates = lines.takeLast(12).mapNotNull { line ->
            val lower = line.lowercase()
            if (isMetadataLine(line) && !TOTAL_LABEL_REGEX.containsMatchIn(lower)) {
                return@mapNotNull null
            }
            findTrailingPrice(line)?.let { parsePrice(it) }
        }

        if (candidates.isEmpty()) return null

        if (productPrices.isEmpty()) {
            return candidates.maxOrNull()
        }

        val aboveProductPrices = candidates.filter { it !in productPriceSet }
        return aboveProductPrices.maxOrNull() ?: candidates.maxOrNull()
    }

    private fun extractDate(text: String, lines: List<String>): String {
        lines.firstOrNull { DATE_LINE_REGEX.matches(it.trim()) }
            ?.let { DATE_LINE_REGEX.find(it)?.groupValues?.get(1) }
            ?.let { return it }

        return DATE_REGEX.find(text)?.value ?: ""
    }

    private fun inferCategoryFromProducts(products: List<ProductDto>): String {
        if (products.isEmpty() || products.all { it.nombre.equals(FALLBACK_PRODUCT_NAME, ignoreCase = true) }) {
            return "General"
        }

        val combined = products.joinToString(" ") { it.nombre }.lowercase()
        val scores = mapOf(
            "Alimentación" to FOOD_KEYWORDS.count { combined.contains(it) },
            "Bebidas" to DRINK_KEYWORDS.count { combined.contains(it) },
            "Hogar" to HOME_KEYWORDS.count { combined.contains(it) },
            "Tecnología" to TECH_KEYWORDS.count { combined.contains(it) }
        )

        val best = scores.maxByOrNull { it.value }
        return if (best != null && best.value > 0) best.key else "General"
    }
}
