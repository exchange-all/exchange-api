package com.exchange.orderbook.service

import org.junit.jupiter.api.Test
import java.util.*

/**
 * @author thaivc
 * @since 2023
 */
class TreeNode(var priority: Int, var value: Int) : Comparable<TreeNode> {
    override fun compareTo(other: TreeNode): Int {
        return this.priority.compareTo(other.priority)
    }

}

class MatchingEngineTest {

    @Test
    fun `test matching one head-bid and one head-ask `() {
        val asks = TreeSet<TreeNode>()
        // 10, 50, 30, 20, 40
        asks.addAll(
            listOf(
                TreeNode(0, 5),
                TreeNode(1, 10),
                TreeNode(2, 50),
                TreeNode(3, 30),
                TreeNode(4, 20),
                TreeNode(5, 40)
            )
        )
        val bids = TreeSet<TreeNode>()
        // 25, 40, 10
        bids.addAll(listOf(TreeNode(0, 5), TreeNode(1, 25), TreeNode(2, 40), TreeNode(3, 10)))

        while (true) {
            if (asks.isEmpty() || bids.isEmpty()) {
                println("END TEST!!!")
                println("remain-asks: ${asks.map { it.value }}")
                println("remain-bids: $bids")
                return
            }
            val askHead = asks.first()
            val bidHead = bids.first()

            if (askHead.value > bidHead.value) {
                println("askHead: ${askHead.value} > bidHead: ${bidHead.value} | remain askHead: ${askHead.value - bidHead.value}")
                bids.remove(bidHead)
                askHead.value -= bidHead.value
                bidHead.value = 0
            }
            if (askHead.value < bidHead.value) {
                println("askHead: ${askHead.value} < bidHead: ${bidHead.value} | remain bidHead: ${bidHead.value - askHead.value}")
                asks.remove(askHead)
                bidHead.value -= askHead.value
                askHead.value = 0
            }

            if (askHead.value == bidHead.value) {
                println("askHead: ${askHead.value} == bidHead: ${bidHead.value}")
                asks.remove(askHead)
                bids.remove(bidHead)
                askHead.value = 0
                bidHead.value = 0
            }
        }
    }

}
