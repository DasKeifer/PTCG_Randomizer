# The Pokemon Trading Card Game Randomizer
A randomizer in the works for the pokemon trading card game. No offical release yet as I'm still settin up infrastructure to support all the planned features to make things go smoother in the future.

## Backstory
I recently got into randomizers for Pokemon mainline games. I've always loved the PTCG on GBC so I looked for a randomizer for it but didn't find any with a  wide bredth of options so I decided to tackle the challenge myself.

Its a project of love for me so I'm not sure when updates and fixes will come out.

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
	* [ ] Update discard energy types if possible. If not Type lock them?
* [ ] Seed & Log
	* [ ] Set & Log Seed
	* [ ] Optionally Log Changes
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
* [ ] Trades
	* [ ] Random/Shuffle
	* [ ] Any Card
		* [ ] Make trade cards promotional?
* [ ] Misc tweaks
	* [ ] Include Pomo cards in booster packs
		* [ ] Exclude 4 Legendary cards from packs
	* [ ] Remove Evolutions
	* [ ] Unlimited trades
	
## Credits: 
This code was originally based on the following projects and has pulled inspiration from them:
* [pocketcg](https://github.com/xCrystal/poketcg)
* [TCGRandomizer](https://github.com/xCrystal/TCGRandomizer)
* [UniversalCardGameRandomizer](https://github.com/anmart/UniversalCardGameRandomizer)
