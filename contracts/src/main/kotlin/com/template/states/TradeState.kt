package com.template.states

import com.template.contracts.NegotiationContract
import net.corda.core.contracts.BelongsToContract
import net.corda.core.contracts.LinearState
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.identity.AbstractParty
import net.corda.core.identity.Party

//@BelongsToContract()
@BelongsToContract(NegotiationContract::class)
data class TradeState(
        val amount: Int,
        val buyer: Party,
        val seller: Party
): LinearState{
    override val linearId: UniqueIdentifier
        get(){
            return UniqueIdentifier();
        }
    override val participants: List<AbstractParty>
        get() {
            return listOf(buyer, seller);
        }
}