# The Pokemon Trading Card Game Randomizer
A randomizer in the works for the pokemon trading card game. No official release yet as I'm still setting up infrastructure to support all the planned features to make things go smoother in the future.

## Backstory
I recently got into randomizers for Pokemon mainline games. I've always loved the PTCG on GBC so I looked for a randomizer for it but didn't find any with a  wide breadth of options so I decided to tackle the challenge myself.

Its a project of love for me so I'm not sure when updates and fixes will come out.

## Planned and Completed Features (in rough priority order)
Some notes on terminology:

*Shuffle* - All existing items will be pooled together and redistributed. Each item will go exactly one place which may or may not be the original place

*Randomize* - All existing items will be gathered. Items then will be randomly assigned and can be used any number of times or not at all
 * [ ] Create Friendly GUI
 * [X] Selecting Rom input and output names/paths
 * [ ] Card Attacks/Poke Powers/Effects
	* [X] Update/Replace Pokemon Names in Attacks/Powers
 	* [X] Randomize Number of Attacks for Cards
 	* [X] Fully Random/Shuffle Attacks
		* [X] Within types
		* [ ] Change to Card Type
	* [ ] Fully Random/Shuffle Poke Powers
		* [ ] Within types
		* [ ] Randomize trainer effects with pokepowers 
	* [ ] Update/Replace energy type specific effects (e.g. ember (Charmander), energy trans (Venusaur))
	* [ ] Update/Replace pokemon specific effects (e.g. call for family (nidoran))
	* [ ] Update/Replace boyfriends (nidoqueen) with random, same type, 3rd stage evo
* [ ] Seed & Log
	* [ ] Set & Log Seed
	* [ ] Optionally Log Changes
* [ ] HP, Retreat Cost 
	* [ ] Fully Random/Shuffle
	* [ ] Random Percentage Based/User Inputtable per evo stage and per type
	* [ ] Evo Stage Weighted
	* [ ] Swap stats to make higher evos more powerful
	* [ ] Evo Chain Consistent (e.g. some chains have generally higher HP, others have generally lower HP)
* [ ] Weakness and Resistance
	* [ ] Fully Random/Shuffle
		* [ ] Make Consistent across "types" (e.g. "Rock" or "Fighting" weakness and resistance)
		* [ ] Make Consistent across Evo
		* [ ] Allow colorless weakness
	* [ ] Percentage Based/User Inputtable
* [ ] Randomize Pokemon Types
	* [ ] Fully Random
	* [ ] Percentage Based/User Inputtable
	* [ ] Consistent in evo lines
* [ ] Randomize Pokemon Evolutions
	* [ ] Full Random/Shuffle
		* [ ] Only change poke with existing Evos
		* [ ] Shuffle within stages
		* [ ] Force Changes
	* [ ] Percentage Based/User Inputtable of each stage
	* [ ] reordering in "pokedex" to group them correctly in deck editor
* [ ] "Power" Based Moves
	* [ ] Create power levels for cards, effects and damage
	* [ ] Assign moves based on card power levels
	* [ ] Randomize power levels of cards based on stage, apply appropriate rarities
	* [ ] Semi order (weight) through evo chains so later evos have higher powered moved
* [ ] Decks
	* [ ] Full Random
		* [ ] Keep Same Number of Card Categories (Energies, Trainers, Pokes)
	* [ ] Random Type Themed
	* [ ] Random Multitype Themed
	* [ ] Percentage Based/User Inputtable
* [ ] Move Generation
	* [ ] Fully random moves effects and powers, and energy costs
	* [ ] Generate moves based on power levels
	* [ ] Include trainer effects
* [ ] Trainer Pics
* [ ] Trades/Promo cards
	* [ ] Random/Shuffle
		* [ ] Within Promos
		* [ ] Any card
* [ ] Miscellaneous tweaks
	* [ ] Make all Colorless
	* [X] Fix card name misspellings (Ninetails vs Ninetales)
	* [ ] Remove Evolutions (all basic)
	* [ ] Include Promo cards in booster packs
		* [ ] Exclude 4 Legendary cards from packs
	* [ ] Unlimited trades?
	
## Credits: 
This code was originally based on the following projects and has pulled inspiration from them:
* [pocketcg](https://github.com/xCrystal/poketcg)
* [TCGRandomizer](https://github.com/xCrystal/TCGRandomizer)
* [UniversalCardGameRandomizer](https://github.com/anmart/UniversalCardGameRandomizer)
