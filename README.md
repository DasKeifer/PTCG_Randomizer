# The Pokemon Trading Card Game Randomizer

## Planned and Completed Features (in rough priority order)
Some notes on terminology:
*Shuffle* - All existing items will be pooled together and redistributed. Each item will go exactly one place which may or may not be the original place
*Randomize* - All existing items will be gathered. Items then will be randomly assigned and can be used any number of times or not at all
 * [ ] Create Friendly GUI
 * [ ] Selecting Rom input and output names/paths
 * [ ] Card Attacks/Poke Powers/Effects
	* [X] Update/Replace Pokemon Names in Attacks/Powers
 	* [ ] Fully Random/Shuffle Attacks
		* [ ] Make all Colorless
		* [ ] Change to Card Type
		* [ ] Assign to Match Card Types
	* [ ] Include Pokepowers
	* [ ] Fully Random/Shuffle Pokepowers Separately
	* [ ] Include Trainer Effects
	* [ ] Include Trainers
	* [ ] Fully Random/Shuffle Trainers Separately
* [ ] Remove Evolutions
* [ ] HP, Retreat Cost 
	* [ ] Fully Random/Shuffle
	* [ ] Random Percentage Based/User Inputtable
	* [ ] Evo Stage Weighted
	* [ ] Evo Chain Consistent
	* [ ] Swap stats to make higher evos more powerful
* [ ] Weakness and Resistance
	* [ ] Fully Random/Shuffle
		* [ ] Make Consistnent across Evo
	* [ ] Random (e.g "Rock" or "Fighting" type weakness and resistances)
		* [ ] Match Card Type
		* [ ] Make Consistnent across Evo
	* [ ] Percentage Based/User Inputtable
* [ ] Generate moves
	* [ ] Fully random moves effects and powers, and energy costs
	* [ ] Include trainer effects
* [ ] Randomize Pokemon Types
	* [ ] Fully Random
	* [ ] Percentage Based/User Inputtable
	* [ ] Consistent in evo lines
* [ ] Randomize Pokemon Evolutions
	* [ ] Full Random/Shuffle
		* [ ] Only existing Evos
		* [ ] Shuffle within stages
		* [ ] Force Changes
	* [ ] Percentage Based/User Inputtable of each stage
	* [ ] reordering in "pokedex" to group them correctly in deck editor
* [ ] Decks
	* [ ] Full Random
		* [ ] Keep Same Number of Card Categories (Energies, Trainers, Pokes)
	* [ ] Random Type Themed
	* [ ] Random Multitype Themed
	* [ ] Pecentage Based/User Inputtable
* [ ] "Power" Based
	* [ ] Create powerlevels for cards, effects and power
	* [ ] Generate moves based on power levels
	* [ ] Assign moves based on card power levels
	* [ ] Randomize power levels of cards based on stage, apply appropiate rarities
* [ ] Trainer Pics
	
* [ ]Misc tweaks
	* [ ] Update discard energy types if possible. If not Type lock them?
	* [ ] Include Pomo cards in booster packs (inlcuding and excluding elite 4 promos)

## Credits: 
This code was originally based on the following projects and has pulled inspiration from them:
* [pocketcg](https://github.com/xCrystal/poketcg)
* [TCGRandomizer](https://github.com/xCrystal/TCGRandomizer)
* [UniversalCardGameRandomizer](https://github.com/anmart/UniversalCardGameRandomizer)
