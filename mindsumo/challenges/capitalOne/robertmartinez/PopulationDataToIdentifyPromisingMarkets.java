package mindsumo.challenges.capitalOne.robertmartinez;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import au.com.bytecode.opencsv.CSVReader;

/**
 * PopulationDataToIdentifyPromisingMarkets.java
 * 
 * @author: Robert Martinez - rmartinezpaez@luc.edu
 *          MS Software Engineering student
 *          Loyola University Chicago
 *          Jan 30, 2014
 */
public class PopulationDataToIdentifyPromisingMarkets
{
	private static Map< String, List< CityInfoDTO > > statesAndCities;
	private static CSVReader reader;
	private static int TOP_NUMBER = 5;

	public static void main( String[ ] args )
	{
		identifyPromisingMarkets( );
	}

	private static void identifyPromisingMarkets( )
	{
		statesAndCities = new HashMap< String, List< CityInfoDTO > >( );
		List< List< CityInfoDTO >> highestPopulationCitiesTargets = identifyHighestPopulationCitiesTargets( );
		List< CityInfoDTO > mSP = highestPopulationCitiesTargets.get( 0 );
		List< CityInfoDTO > hPG = highestPopulationCitiesTargets.get( 1 );
		List< StateInfoDTO > sWHCG = identifyStatesWithHighestCumulativeGrowth( );
		showResults( mSP, hPG, sWHCG );
	}

	private static List< List< CityInfoDTO >> identifyHighestPopulationCitiesTargets( )
	{
		// hPG : highestPopulationGrowht, mSP : mostShrinkingPopulation
		List< CityInfoDTO > hPG = new ArrayList< CityInfoDTO >( );
		List< CityInfoDTO > mSP = new ArrayList< CityInfoDTO >( );

		try
		{
			reader = new CSVReader( new FileReader( "Metropolitan_Populations__2010-2012_.csv" ) );
			reader.readNext( ); // Avoid the titles row

			String[ ] nextLine;
			while ( ( nextLine = reader.readNext( ) ) != null )
			{
				String geography = nextLine[ 0 ];
				Double population2010 = Double.parseDouble( nextLine[ 1 ] );
				Double population2012 = Double.parseDouble( nextLine[ 3 ] );

				String[ ] cityAndState = ( geography.contains( ";" ) ) ? geography.split( ";" ) : geography.split( "," );

				String city = cityAndState[ 0 ].trim( );
				String state = cityAndState[ 1 ].trim( );
				Double change = ( population2010 > 0 ) ? ( ( population2012 * 100 ) / population2010 ) - 100 : 0;

				CityInfoDTO cityInfoDTO = new CityInfoDTO( city, state, change, population2010, population2012 );

				if ( population2010 > 49999 )
				{
					hPG = selectHPG( hPG, cityInfoDTO );
					mSP = selectMSP( mSP, cityInfoDTO );
				}

				List< CityInfoDTO > citiesList = statesAndCities.get( state );
				if ( citiesList == null )
					citiesList = new ArrayList< CityInfoDTO >( );
				citiesList.add( cityInfoDTO );
				statesAndCities.put( state, citiesList );
			}
		}
		catch ( FileNotFoundException e )
		{
			e.printStackTrace( );
		}
		catch ( IOException e )
		{
			e.printStackTrace( );
		}
		return Arrays.asList( mSP, hPG );
	}

	private static List< CityInfoDTO > selectHPG( List< CityInfoDTO > hPG, CityInfoDTO cityInfoDTO )
	{
		if ( hPG.isEmpty( ) )
		{
			hPG.add( cityInfoDTO );
		}
		else
		{
			int hPGSize = ( hPG.size( ) > TOP_NUMBER ) ? TOP_NUMBER : hPG.size( );
			for ( int i = 0; i < hPGSize; i++ )
			{
				if ( cityInfoDTO.compareTo( hPG.get( i ) ) >= 0 && !hPG.contains( cityInfoDTO ) )
				{
					hPG.add( i, cityInfoDTO );
					if ( hPG.size( ) >= TOP_NUMBER )
					{
						hPG = hPG.subList( 0, TOP_NUMBER );
					}
					i = hPGSize;
				}
				else if ( hPGSize < TOP_NUMBER && !hPG.contains( cityInfoDTO ) )
				{
					hPG.add( cityInfoDTO );
					Collections.sort( hPG );
					Collections.reverse( hPG );
					if ( hPG.size( ) >= TOP_NUMBER )
					{
						hPG = hPG.subList( 0, TOP_NUMBER );
					}
				}
			}
		}
		return hPG;
	}

	private static List< CityInfoDTO > selectMSP( List< CityInfoDTO > mSP, CityInfoDTO cityInfoDTO )
	{
		if ( mSP.isEmpty( ) )
		{
			mSP.add( cityInfoDTO );
		}
		else
		{
			int mSPSize = ( mSP.size( ) > TOP_NUMBER ) ? TOP_NUMBER : mSP.size( );
			for ( int i = 0; i < mSPSize; i++ )
			{
				if ( cityInfoDTO.compareTo( mSP.get( i ) ) <= 0 && !mSP.contains( cityInfoDTO ) )
				{
					mSP.add( i, cityInfoDTO );
					if ( mSP.size( ) >= TOP_NUMBER )
					{
						mSP = mSP.subList( 0, TOP_NUMBER );
					}
					i = mSPSize;
				}
				else if ( mSPSize < TOP_NUMBER && !mSP.contains( cityInfoDTO ) )
				{
					mSP.add( cityInfoDTO );
					Collections.sort( mSP );
					if ( mSP.size( ) >= TOP_NUMBER )
					{
						mSP = mSP.subList( 0, TOP_NUMBER );
					}
				}
			}
		}
		return mSP;
	}

	private static List< StateInfoDTO > identifyStatesWithHighestCumulativeGrowth( )
	{
		// sWHCG : statesWithHighestCumulativeGrowth initials
		List< StateInfoDTO > sWHCG = new ArrayList< StateInfoDTO >( );
		Iterator< Entry< String, List< CityInfoDTO >>> iterator = statesAndCities.entrySet( ).iterator( );
		while ( iterator.hasNext( ) )
		{
			Entry< String, List< CityInfoDTO >> state = iterator.next( );
			StateInfoDTO stateGrowth = stateGrowth( state.getValue( ) );
			sWHCG = selectSWHCG( sWHCG, stateGrowth );
		}
		return sWHCG;
	}

	private static StateInfoDTO stateGrowth( List< CityInfoDTO > stateCitiesList )
	{
		double population2010 = 0;
		double population2012 = 0;

		for ( CityInfoDTO cityInfoDTO : stateCitiesList )
		{
			population2010 += cityInfoDTO.population2010;
			population2012 += cityInfoDTO.population2012;
		}

		double change = ( population2010 > 0 ) ? ( ( population2012 * 100 ) / population2010 ) - 100 : 0;

		return new StateInfoDTO( stateCitiesList.get( 0 ).state, change );
	}

	private static List< StateInfoDTO > selectSWHCG( List< StateInfoDTO > sWHCG, StateInfoDTO stateGrowth )
	{
		if ( sWHCG.isEmpty( ) )
		{
			sWHCG.add( stateGrowth );
		}
		else
		{
			int sWHCGSize = ( sWHCG.size( ) > TOP_NUMBER ) ? TOP_NUMBER : sWHCG.size( );
			for ( int i = 0; i < sWHCGSize; i++ )
			{
				if ( stateGrowth.compareTo( sWHCG.get( i ) ) >= 0 && !sWHCG.contains( stateGrowth ) )
				{
					sWHCG.add( i, stateGrowth );
					if ( sWHCG.size( ) >= TOP_NUMBER )
					{
						sWHCG = sWHCG.subList( 0, TOP_NUMBER );
					}
					i = sWHCGSize;
				}
				else if ( sWHCGSize < TOP_NUMBER && !sWHCG.contains( stateGrowth ) )
				{
					sWHCG.add( stateGrowth );
					Collections.sort( sWHCG );
					Collections.reverse( sWHCG );
					if ( sWHCG.size( ) >= TOP_NUMBER )
					{
						sWHCG = sWHCG.subList( 0, TOP_NUMBER );
					}
				}
			}
		}
		return sWHCG;
	}

	private static void showResults( List< CityInfoDTO > mSP, List< CityInfoDTO > hPG, List< StateInfoDTO > sWHCG )
	{
		DecimalFormat df = new DecimalFormat( "0.00" );
		showCitiesResults( hPG, df, true );
		showCitiesResults( mSP, df, false );
		showSWHCGResults( sWHCG, df );
	}

	private static void showCitiesResults( List< CityInfoDTO > citiesList, DecimalFormat df, boolean hPGList )
	{
		int citiesNumber = ( TOP_NUMBER <= citiesList.size( ) ) ? TOP_NUMBER : citiesList.size( );
		if ( hPGList )
		{
			System.out.println( "\n1)  Highest Population Growth ( Top " + citiesNumber + " - Cities with population greater than or equal 50.000 at 2010 )\n" );
		}
		else
		{
			System.out.println( "\n\n2)  Most Shrinking Population ( Top " + citiesNumber + " - Cities with population greater than or equal 50.000 at 2010 )\n" );
		}
		for ( int i = 0; i < citiesNumber; i++ )
		{
			String format = String.format( "%-40s %s", ( i + 1 ) + ". " + citiesList.get( i ).name + ", " + citiesList.get( i ).state, " -> change: " + df.format( citiesList.get( i ).change ) + "%" );
			System.out.println( format );
		}
	}

	private static void showSWHCGResults( List< StateInfoDTO > statesList, DecimalFormat df )
	{
		int statesNumber = ( TOP_NUMBER <= statesList.size( ) ) ? TOP_NUMBER : statesList.size( );
		System.out.println( "\n\n3)  States with highest cumulative growth ( Top " + statesNumber + " )\n" );
		for ( int i = 0; i < statesNumber; i++ )
		{
			String format = String.format( "%-40s %s", ( i + 1 ) + ". " + statesList.get( i ).name, " -> change: " + df.format( statesList.get( i ).change ) + "%" );
			System.out.println( format );
		}
	}

	private static class StateInfoDTO implements Comparable< StateInfoDTO >
	{
		private String name;
		private Double change;

		public StateInfoDTO( String name, Double change )
		{
			super( );
			this.name = name;
			this.change = change;
		}

		@Override
		public int compareTo( StateInfoDTO o )
		{
			return this.change < o.change ? -1 : this.change > o.change ? 1 : 0;
		}
	}

	private static class CityInfoDTO implements Comparable< CityInfoDTO >
	{
		private String name;
		private String state;
		private Double change;
		private Double population2010;
		private Double population2012;

		public CityInfoDTO( String name, String state, Double change, Double population2010, Double population2012 )
		{
			super( );
			this.name = name;
			this.state = state;
			this.change = change;
			this.population2010 = population2010;
			this.population2012 = population2012;
		}

		@Override
		public int compareTo( CityInfoDTO o )
		{
			return this.change < o.change ? -1 : this.change > o.change ? 1 : 0;
		}
	}
}