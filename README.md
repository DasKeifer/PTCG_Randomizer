# The Pokemon Trading Card Game Randomizer
A randomizer in the works for the pokemon trading card game. No official release yet as I'm still setting up infrastructure to support all the planned features to make things go smoother in the future.

## Backstory
I recently got into randomizers for Pokemon mainline games. I've always loved the PTCG on GBC so I looked for a randomizer for it but didn't find any with a  wide breadth of options so I decided to tackle the challenge myself.

Its a project of love for me so I'm not sure when updates and fixes will come out.

## Planned and Completed Features (in rough priority order)
Some notes on terminology:

*Shuffle* - All items are pooled together and selected and removed from the pool. If the pool is empty, it will be reset. Each item will be used once before an item is used a second time </br>
*Randomize* - All items are pooled together and randomly chosen from. Items may be used many times or not at all

Major Feature List and Roadmap Key: </br>
⚪ Not implemented </br>
🔘 Present in latest major release </br>
🟢 Added in develop to be included in next major release </br>
🔵 Planned to be included in next major release </br>
🟡 Candidates to be included in next major release </br>
Features that end with a "?" are ones that may or may not be feasible to do that need to be looked into further
```
🟢 Create a basic friendly GUI to expand upon as features are added
🟢 Selecting Rom input and output names/paths </br>
🔵 Card Attacks/Poke Powers/Effects
    🟢 Update/Replace Pokemon names in attacks/powers </br>
        🟢 Fully random/shuffle attacks
	    🟢 Within types
	    🟢 Change type energies to card type
	🟢 Fully random/shuffle Poke Powers
	    🟢 Include with moves
	    🟢 Within types
 	🟢 Randomize number of attacks for cards (based on original average moves per card)
🟢 Seed & Log
    🟢 Set seed
    🟢 Log seed
    🟢 Optionally log changes
🟡 Advance Move Tweaks
    🟡 Update/Replace energy type specific effects (e.g. ember (Charmander), energy trans (Venusaur))
    🟡 Update/Replace pokemon specific effects (e.g. call for family (nidoran))
    🟡 Update/Replace boyfriends (attack of nidoqueen) with random, same type, 3rd stage evo
    🟡 Randomize trainer effects with poke Powers?
⚪ HP, Retreat Cost 
    ⚪ Fully random/shuffle
    ⚪ Evo chain progressing - swap stats to make higher evos more powerful/higher retreat cost
    ⚪ Evo chain consistent - some chains have generally higher HP, others have generally lower HP)
⚪ Weakness and Resistance
    ⚪ Fully random/shuffle
        ⚪ Make consistent across "types" (e.g. "Rock" or "Fighting" weakness and resistance)
        ⚪ Evo chain consistent - chains generally have the same weaknessess and resistances
        ⚪ Allow colorless weakness?
    ⚪ Multiple weaknesses/resistances
⚪ "Power" Based Moves (more balanced randomization)
    ⚪ Create power levels for cards, effects and damage
    ⚪ Assign moves based on card power levels
    ⚪ Randomize power levels of cards based on stage, apply appropriate rarities
    ⚪ Semi order (weight) through evo chains so later evos have higher powered moved
⚪ Randomize Pokemon Types
    ⚪ Fully random
    ⚪ Consistent in evo lines (done after randomizing evo lines)
⚪ Randomize Pokemon Evolutions
    ⚪  Fully random/shuffle
        ⚪ Only change poke with existing Evos
        ⚪ Shuffle within stages
        ⚪ Force changes
    ⚪ Reassigning pokedex numbers to group them correctly in deck editor
    ⚪ Remove evolutions (all basic)
⚪ Decks
    ⚪ Random "type"/energy themed
    ⚪ Random multitype/energy themed
    ⚪ Random Keep same number of card categories (energies, trainers, pokes)
    ⚪ Evolution sanity
⚪ Advance & Customizable General Randomizations
    ⚪ Additional knobs for forcing damaging moves
    ⚪ User specifyable "types" - weakness/resistance pairs
    ⚪ User specified HP & retreat cost ranges
    ⚪ Per energy percentage based, user inputtable Pokemon types distributions
    ⚪ Per stage percentage based, user input-able evolutions distributions 
    ⚪ Per stage/evos left & per HP value percentage based, user input-able HP distributions 
    ⚪ Per stage/evos left & per retreat cost percentage based, user input-able retreat cost distributions 
    ⚪ Per stage/evos left percentage based, user input-able number of attacks/Poke Powers distributions
    ⚪ Per "type" percentage based, user inputtable weakness/resistance distributions
    ⚪ Percentage based/user inputtable decks (knobs TBD)
⚪ Move Generation
    ⚪ Generate moves based on power levels	
		⚪ User tweakable power scaling
    ⚪ Smart name generation
        ⚪ User inputtable names optionally type specific
    ⚪ Include trainer effects
⚪ Trainer Pics
⚪ Trades/Promo Cards
    ⚪ Fully Random
        ⚪ Within promos
        ⚪ Any card
⚪ Miscellaneous Tweaks
    🟢 Make all colorless
    🟢 Fix card name spelling errors (Ninetails vs Ninetales)
    ⚪ Booster pack adjustements
        ⚪ Change number of cards in booster packs (max 11)
        🟡 Change number of packs awarded after win (min ?, max ?)
        ⚪ Change card rarity distribution
    ⚪ Include promo cards in booster packs?
        ⚪ Exclude 4 legendary cards from packs
    ⚪ Unlimited trades?
```
## Credits: 
This code heavily draws from past work by the following projects particularly pret's poketcg decompilation and annotations of the game files:
* [pocketcg](https://github.com/pret/poketcg)
* [TCGRandomizer](https://github.com/xCrystal/TCGRandomizer)
* [UniversalCardGameRandomizer](https://github.com/anmart/UniversalCardGameRandomizer)
