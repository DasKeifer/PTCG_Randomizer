# The Pokemon Trading Card Game Randomizer
A randomizer in the works for the pokemon trading card game. No official release yet as I'm still setting up infrastructure to support all the planned features to make things go smoother in the future.

## Backstory
I recently got into randomizers for Pokemon mainline games. I've always loved the PTCG on GBC so I looked for a randomizer for it but didn't find any with a  wide breadth of options so I decided to tackle the challenge myself.

Its a project of love for me so I'm not sure when updates and fixes will come out.

## Planned and Completed Features (in rough priority order)
Some notes on terminology:

*Shuffle* - All existing items will be pooled together and redistributed. Each item will go exactly one place which may or may not be the original place

*Randomize* - All existing items will be gathered. Items then will be randomly assigned and can be used any number of times or not at all
 * [X] Create a basic friendly GUI to expand upon as features are added
 * [X] Selecting Rom input and output names/paths
 * [ ] Card Attacks/Poke Powers/Effects
	* [X] Update/Replace Pokemon names in attacks/powers
 	* [X] Fully random/shuffle attacks
		* [X] Within types
		* [X] Change type energies to card type
	* [X] Fully random/shuffle Poke Powers
		* [X] Include with moves
		* [X] Within types
	* [ ] Advanced move randomization options (more balanced randomization)
		* [ ] Randomize moves within card evo tiers 
		* [ ] Randomize attacks based on energy cost (replace a 2 energy move with another one)
 	* [ ] Randomize number of attacks for cards (need to add switch in GUI still)
* [X] Seed & Log
	* [X] Set seed
	* [X] Log seed
	* [X] Optionally log changes
* [ ] HP, Retreat Cost 
	* [ ] Fully random/shuffle
	* [ ] Swap stats to make higher evos more powerful/higher retreat cost
	* [ ] Evo chain consistent (e.g. some chains have generally higher HP, others have generally lower HP)
* [ ] Weakness and Resistance
	* [ ] Fully random/shuffle
		* [ ] Make consistent across "types" (e.g. "Rock" or "Fighting" weakness and resistance)
		* [ ] Make consistent across Evo (done after randomizing evo lines)
		* [ ] Allow colorless weakness?
	* [ ] Multiple weaknesses/resistances?
* [ ] Randomize Pokemon Types
	* [ ] Fully random
	* [ ] Consistent in evo lines (done after randomizing evo lines)
* [ ] Randomize Pokemon Evolutions
	* [ ] Fully random/shuffle
		* [ ] Only change poke with existing Evos
		* [ ] Shuffle within stages
		* [ ] Force changes
	* [ ] Reordering in "pokedex" to group them correctly in deck editor
	* [ ] Remove evolutions (all basic)
* [ ] Decks
	* [ ] Fully random
	* [ ] Random "type"/energy themed
	* [ ] Random multitype/energy themed
	* [ ] Keep same number of card categories (energies, trainers, pokes)
	* [ ] Evolution sanity
* [ ] Advance & Customizable General Randomizations
	* [ ] User specifyable "types" - weakness/resistance pairs
	* [ ] User specified HP & retreat cost ranges
	* [ ] Per energy percentage based, user inputtable Pokemon types distributions
	* [ ] Per stage percentage based, user input-able evolutions distributions 
	* [ ] Per stage/evos left & per HP value percentage based, user input-able HP distributions 
	* [ ] Per stage/evos left & per retreat cost percentage based, user input-able retreat cost distributions 
	* [ ] Per stage/evos left percentage based, user input-able number of attacks/Poke Powers distributions
	* [ ] Per "type" percentage based, user inputtable weakness/resistance distributions
	* [ ] Percentage based/user inputtable decks (knobs TBD)
* [ ] Advance Card Attacks/Poke Powers/Effects
	* [ ] Randomize trainer effects with poke Powers 
	* [ ] Update/Replace energy type specific effects (e.g. ember (Charmander), energy trans (Venusaur))
	* [ ] Update/Replace pokemon specific effects (e.g. call for family (nidoran))
	* [ ] Update/Replace boyfriends (nidoqueen) with random, same type, 3rd stage evo
* [ ] "Power" Based Moves
	* [ ] Create power levels for cards, effects and damage
	* [ ] Assign moves based on card power levels
	* [ ] Randomize power levels of cards based on stage, apply appropriate rarities
	* [ ] Semi order (weight) through evo chains so later evos have higher powered moved
* [ ] Move Generation
	* [ ] Fully random moves effects and powers, and energy costs
	* [ ] Generate moves based on power levels
	* [ ] Include trainer effects
* [ ] Trainer Pics
* [ ] Trades/Promo Cards
	* [ ] Fully Random
		* [ ] Within promos
		* [ ] Any card
* [ ] Miscellaneous Tweaks
	* [X] Make all colorless
	* [X] Fix card name spelling errors (Ninetails vs Ninetales)
	* [ ] Adjust cards in booster packs
		* [ ] Make booster packs contain eleven cards (always done now)
		* [ ] Additional booster packs
	* [ ] Include promo cards in booster packs?
		* [ ] Exclude 4 legendary cards from packs
	* [ ] Unlimited trades?
	
## Credits: 
This code heavily draws from past work by the following projects particularly pret's poketcg decompilation and annotations of the game files:
* [pocketcg](https://github.com/pret/poketcg)
* [TCGRandomizer](https://github.com/xCrystal/TCGRandomizer)
* [UniversalCardGameRandomizer](https://github.com/anmart/UniversalCardGameRandomizer)
