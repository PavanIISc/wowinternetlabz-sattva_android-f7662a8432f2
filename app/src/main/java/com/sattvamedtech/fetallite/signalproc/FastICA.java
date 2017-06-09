package com.sattvamedtech.fetallite.signalproc;

import java.util.Random;

public class FastICA
{
	public double[][] ICA(double[][] MixedSignal1)
	{
		

		int Number_of_IC = MixedSignal1.length;
		int Samples = MixedSignal1[0].length;
		int count = 0;
		if (Number_of_IC > Samples)
		{
			Samples = Number_of_IC;
			Number_of_IC = MixedSignal1[0].length;
			count = 1;
		}
		double[][] MixedSignal = new double[Number_of_IC][Samples];
		if (count == 1)
		{
			for (int i =0; i<Number_of_IC; i++)
			{
				for (int j = 0; j<Samples; j++)
				{
					MixedSignal[i][j] = (float)MixedSignal1[j][i];
				}
			}
		}else
		{
			for (int i =0; i<Number_of_IC; i++)
			{
				for (int j = 0; j<Samples; j++)
				{
					MixedSignal[i][j] = (float)MixedSignal1[i][j];
				}
			}
		}
		
		/**
		 * OUTPUTS
		 */
		
		float[][] A = new float[Number_of_IC][Number_of_IC];
		float[][] W = new float[Number_of_IC][Number_of_IC];
		float[][] Inp_ZeroMean = new float[Number_of_IC][Samples];
		/**
		 * Make the input zero mean
		 */

		float[] mean = new float[Number_of_IC];
		
		for (int i = 0; i< Samples; i++)
		{
			mean[0] = mean[0] + (float)MixedSignal[0][i];
			mean[1] = mean[1] + (float)MixedSignal[1][i];
			mean[2] = mean[2] + (float)MixedSignal[2][i];
			mean[3] = mean[3] + (float)MixedSignal[3][i];
		}
		
		mean[0] = mean[0]/Samples;
		mean[1] = mean[1]/Samples;
		mean[2] = mean[2]/Samples;
		mean[3] = mean[3]/Samples;
		
		for (int i = 0; i< Samples; i++)
		{
			Inp_ZeroMean[0][i] = (float)MixedSignal[0][i] - mean[0];
			Inp_ZeroMean[1][i] = (float)MixedSignal[1][i] - mean[1];
			Inp_ZeroMean[2][i] = (float)MixedSignal[2][i] - mean[2];
			Inp_ZeroMean[3][i] = (float)MixedSignal[3][i] - mean[3];
		}
		
		/**
		 *  Whitening the matrix : Doing EigValue Decomp
		 *  
		 *  inp_eigen = X*X'/T ; which is Covariance of (X);
		 *  X*X' = U*D*U' ;
		 */
		
		// Do  X*X'
		double[][] inp_eigen = new double[Number_of_IC][Number_of_IC];
		for (int i = 0; i<Number_of_IC; i++)
		{
			for (int j = 0; j<Samples; j++)
			{
				inp_eigen[i][0] = inp_eigen[i][0] + Inp_ZeroMean[i][j]*Inp_ZeroMean[0][j];
				inp_eigen[i][1] = inp_eigen[i][1] + Inp_ZeroMean[i][j]*Inp_ZeroMean[1][j];
				inp_eigen[i][2] = inp_eigen[i][2] + Inp_ZeroMean[i][j]*Inp_ZeroMean[2][j];
				inp_eigen[i][3] = inp_eigen[i][3] + Inp_ZeroMean[i][j]*Inp_ZeroMean[3][j];
			}
			inp_eigen[i][0] = inp_eigen[i][0] / Samples;
			inp_eigen[i][1] = inp_eigen[i][1] / Samples;
			inp_eigen[i][2] = inp_eigen[i][2] / Samples;
			inp_eigen[i][3] = inp_eigen[i][3] / Samples;
		}
		
		
		EigenvalueDecomposition ed=new EigenvalueDecomposition(inp_eigen);
		
	    double[] diag = ed.getRealEigenvalues();
	    double[][] U = ed.getV();
		
		float[][] WhiteningMatrix = new float[Number_of_IC][Number_of_IC];
		float[][] DewhiteningMatrix = new float[Number_of_IC][Number_of_IC];
		float[][] WhiteSignal = new float[Number_of_IC][Samples];
	      /**
	       *  Whitening of the input data 
	       *  Whitematrix  = U^T / sqrt(D);
	       *  DewhiteMatrix = U * sqrt(D);
	       *  WhiteSig = whitematrix* input_zeromean
	       */
		double tempD ;
	      for (int i=0; i<Number_of_IC; i++)
	      {
	    	  for (int j = 0; j<Number_of_IC; j++)
	    	  {
	    		  tempD = Math.sqrt(diag[j]);
	    		  WhiteningMatrix[j][i] = (float)((U[i][j]) / tempD);
	    		  DewhiteningMatrix[i][j] = (float)(U[i][j]*tempD);

	    	  }
	      }
	      for (int i=0; i<Number_of_IC; i++)
	      {
			  for (int k = 0; k<Samples; k++)
			  {
				  for (int j = 0; j<Number_of_IC; j++)
		    	  {
					  WhiteSignal[i][k] = WhiteSignal[i][k] + (float)((U[j][i] * Inp_ZeroMean[j][k] )/ Math.sqrt(diag[i]));
		    	  }
			  }
	      }
	      
	      
	      /**
	       * Initialize for Fast - ICA
	       * 
	       * choice of nonlinearity is 'pow3'
	       * 
	       */
	      
	      int maxNumberOfItertions = 1000;
	      double epsilon = 0.0001;
	      
	      int gOrig = 10;
	      float myy = 1;
	      //int gFine = gOrig + 1;
	      //int FinetuningEnabled = 0;
	      int StabilizationEnabled = 1;
	      float myyOrig = myy;
	      //double myyK = 0.01;
	      int FailureLimit = 5;
	      
	      int usedNonLinearity = gOrig;
	      float stroke = 0;
	      int notFine = 1;
	      int Long = 0;
	      //int InitialStateMode = 0;
	      int endFinetuning = 0;
	      //int gabba = 1;
	      int iter = 1;
	      // DEFLATION APPROACH
	      
	      System.out.println("Starting ICA calculation... ");
	      
	      
	      float[][] B = new float[Number_of_IC][Number_of_IC];
	      //float[][] B = {{1,3,2,4},{2,4,1,5},{1,2,1,4},{2,3,1,2}};
	      //float[][] BBT = new float[Number_of_IC][Number_of_IC];
	      float[] w = new float[Number_of_IC];
	      float[] w1 = new float[Number_of_IC];
	      float[] X = new float[Samples];
	      float[] wOld = new float[Number_of_IC];
	      float[] wOld2 = new float[Number_of_IC];
	      double normW1,normW2;
	      int round = 1;
	      
	      int numFailures = 0;
	      Random randGausian = new Random();
	      double norm = 0;
	      double sum1 = 0;
	      double sum2 = 0;
	      double sum3 = 0;
	      double sum4 = 0;
	      //int count = 0; // for doing B*B^T
	      while (round <= Number_of_IC)
	      {
	    	  myy = myyOrig;
	    	  usedNonLinearity = gOrig;
	    	  stroke = 0;
	    	  notFine = 1;
	    	  Long = 0;
	    	  endFinetuning = 0;
	    	  
	    	 // System.out.println("\nIC "+round);
	    	  // Take a random initial vector of length and orthogonalize it
	    	  // with respect to the other vectors.
	    	  for (int i = 0; i<Number_of_IC; i++)
	    	  {
	    		  w[i] = (float)randGausian.nextGaussian();
	    		  //w[i] = 1+i;
	    	  }
	    	  
	    	  // w = w - B*B' * w
	    	  // w = w/norm(w)
	    	  norm = 0;
	    	  for (int i = 0; i<Number_of_IC; i++)
	    	  {
	    		  
	    		  sum1 = 0;
	    		  sum2 = 0;
	    		  sum3 = 0;
	    		  sum4 = 0;
	    		  for (int j = 0; j<Number_of_IC; j++)
	    		  {
	    			 sum1 = sum1 + B[i][j] * B[0][j] * w[0];
	    			 sum2 = sum2 + B[i][j] * B[1][j] * w[1];
	    			 sum3 = sum3 + B[i][j] * B[2][j] * w[2];
	    			 sum4 = sum4 + B[i][j] * B[3][j] * w[3];
	    		  }
	    		  w1[i] = w[i] - (float)(sum1+sum2+sum3+sum4);
	    		  norm = norm + w1[i]*w1[i];
	    	  }
	    	  norm = Math.sqrt(norm);
	    	  for (int i = 0; i<Number_of_IC; i++)
	    	  {
	    		  w[i] =(float)( w1[i]/norm);
	    	  }
	    	  
	    	  
	    	  // This is the actual fixed-point iteration loop.
	    	  
	    	  iter = 1;
	    	  //gabba = 1;
	    	  
	    	  while (iter <= maxNumberOfItertions )
	    	  {
	    		  // Project the vector into the space orthogonal to the space
	    	      // spanned by the earlier found basis vectors. Note that we can do
	    	      // the projection with matrix B, since the zero entries do not
	    	      // contribute to the projection.
	    		  // w = w - B*B' * w
		    	  // w = w/norm(w)
		    	  norm = 0;
		    	  for (int i = 0; i<Number_of_IC; i++)
		    	  {
		    		  
		    		  sum1 = 0;
		    		  sum2 = 0;
		    		  sum3 = 0;
		    		  sum4 = 0;
		    		  for (int j = 0; j<Number_of_IC; j++)
		    		  {
		    			 sum1 = sum1 + B[i][j] * B[0][j] * w[0];
		    			 sum2 = sum2 + B[i][j] * B[1][j] * w[1];
		    			 sum3 = sum3 + B[i][j] * B[2][j] * w[2];
		    			 sum4 = sum4 + B[i][j] * B[3][j] * w[3];
		    		  }
		    		  w1[i] = w[i] - (float)(sum1+sum2+sum3+sum4);
		    		  norm = norm + w1[i]*w1[i];
		    	  }
		    	  norm = Math.sqrt(norm);
		    	  for (int i = 0; i<Number_of_IC; i++)
		    	  {
		    		  w[i] =(float)( w1[i]/norm);
		    	  }
	    		  
		    	  if (notFine == 1)
		    	  {
		    		  if (iter == maxNumberOfItertions + 1)
		    		  {
		    			  System.out.println("Component number "+round+" did not converge in "+maxNumberOfItertions+" iterations.");
		    			  round = round - 1;
		    			  numFailures = numFailures + 1;
		    			  if (numFailures > FailureLimit)
			    		  {
			    			  System.out.println("Too many failures to converge ("+numFailures+"). Giving up.");
			    			  if (round == 0)
			    			  {
			    				  A = null;
			    				  W = null;
			    			  }
			    			  return null;
			    		  }
			    		  break;
		    		  }
		    		  
		    	  }else
		    	  {
		    		  if (iter >=endFinetuning)
		    		  {
		    			  for (int i = 0; i<Number_of_IC; i++)
		    			  {
		    				  wOld[i] = w[i];  // So the algorithm will stop on the next test...
		    			  }
		    		  }
		    		  
		    	  } // end notFine condition
	    		  
	    		  // Show the progress
		    	 // System.out.println(".");
	    		  
		    	  // Test for termination condition. Note that the algorithm has
		          // converged if the direction of w and wOld is the same, this
		          // is why we test the two cases.
		    	  normW1 = 0;
		    	  normW2 = 0;
		    	  for (int i = 0; i<Number_of_IC; i++)
		    	  {
		    		  normW1 = normW1 + (w[i] - wOld[i])*(w[i] - wOld[i]);
		    		  normW2 = normW2 + (w[i] + wOld[i])*(w[i] + wOld[i]);
		    	  }
		    	  normW1 = Math.sqrt(normW1);
		    	  normW2 = Math.sqrt(normW2);
		    	  
		    	  if (normW1 < epsilon || normW2 < epsilon)
		    	  {
		    		  numFailures = 0;
		    		  for (int i = 0; i<Number_of_IC; i++)
		    		  {
		    			  B[i][round-1] = w[i];
		    			  W[round-1][i] = w[0]*WhiteningMatrix[0][i] + w[1]*WhiteningMatrix[1][i] + w[2]*WhiteningMatrix[2][i] + w[3]*WhiteningMatrix[3][i]; 
		    			  A[i][round-1] = w[0]*DewhiteningMatrix[i][0] + w[1]*DewhiteningMatrix[i][1] + w[2]*DewhiteningMatrix[i][2] + w[3]*DewhiteningMatrix[i][3] ;
		    		  }
		    		  System.out.println("IC "+round+" > Computed in "+iter+" steps");
		    		  break;
		    		  
		    	  }
		    	  
		    	  else if (StabilizationEnabled == 1)
		    	  {
		    		  if (stroke == 0 && normW1 < epsilon || normW2 < epsilon)
		    		  {
		    			  stroke = myy;
		    			  System.out.println("Stroke!");
		    			  myy = (float)0.5*myy;
		    			  
		    			  if ( (usedNonLinearity %2) == 0)
			    		  {
			    			  usedNonLinearity = usedNonLinearity + 1;
			    		  }
		    		  }else if (stroke == 1)
		    		  {
		    			  myy = stroke;
		    			  stroke = 0;
		    			  if (myy == 1 && (usedNonLinearity%2 == 1))
		    			  {
		    				  usedNonLinearity = usedNonLinearity -1;
		    			  }
		    		  } else if ((notFine == 1 )&& (Long ==0) && (iter > maxNumberOfItertions/2))
		    		  {
		    			  System.out.println("Taking long (reducing step size)");
		    			  Long = 1;
		    			  myy = (float)0.5 * myy;
		    			  if (usedNonLinearity %2 == 0)
		    			  {
		    				  usedNonLinearity = usedNonLinearity + 1;
		    			  }
		    		  }
		    		  
		    	  }
		    	  
		    	  for (int i = 0; i<Number_of_IC ; i++)
		    	  {
		    		  wOld2[i] = wOld[i];
		    		  wOld[i] = w[i];
		    	  }
		    	  
		    	  
		    	  for (int i = 0; i<Samples; i++)
		    	  {
		    		  X[i] = WhiteSignal[0][i] * w[0] + WhiteSignal[1][i] * w[1] + WhiteSignal[2][i] * w[2] + WhiteSignal[3][i] * w[3];
		    		  X[i] = X[i]*X[i]*X[i];
		    	  }
		    	  norm = 0;
		    	  for (int i = 0; i<Number_of_IC; i++)
		    	  {
		    		  w1[i] = 0;
		    		  for (int j = 0; j<Samples; j++)
		    		  {
		    			  w1[i] = w1[i] + X[j]*WhiteSignal[i][j];
		    		  }
		    		  w1[i] = w1[i]/Samples - 3* w[i];
		    		  norm = norm + w1[i]*w1[i];
		    	  }
		    		  norm = Math.sqrt(norm);
		    	  for (int i = 0; i<Number_of_IC; i++)
		    	  {
		    		  w[i] = (float) (w1[i]/norm);
		    	  }
		    	  
		    	  iter = iter + 1;
	    	  } // end while maxiteration 
	    		
	    	  round = round + 1;
	    	  
	    	  int k =0;
		      k = k+1;
	    	  
	      }
	      
	      
	      
	      // Estimate the Independent source signals.
	      
	      double[][] icasig = new double[Number_of_IC][Samples];
	      
	      
	      for (int i = 0; i<Number_of_IC; i++)
	      {
	    	  for (int j = 0; j<Samples; j++)
	    	  {
	    		  for (int k = 0; k<Number_of_IC; k++)
	    		  {
	    			  icasig[i][j] = icasig[i][j] + W[i][k] * (MixedSignal[k][j] + mean[i]);
	    		  }
	    		 
	    	  }
	      }
			System.out.println("ICA1 W matrix Out: ");
			System.out.println("1 : " + W[0][0] +" "+ W[0][1] +" " + W[0][2] +" "+ W[0][3]);
			System.out.println("11 : " + W[1][0] +" "+ W[1][1] +" " + W[1][2] +" "+ W[1][3]);
			System.out.println("21 : " + W[2][0] +" "+ W[2][1] +" " + W[2][2] +" "+ W[2][3]);
			System.out.println("31 : " + W[3][0] +" "+ W[3][1] +" " + W[3][2] +" "+ W[3][3]);
	      // Transpose the icasig to output
	      double[][] IcaOut = new double[Samples][Number_of_IC];
	      for (int i = 0; i<Samples; i++)
	      {
	    	  for (int j=0; j<Number_of_IC; j++)
	    	  {
	    		  IcaOut[i][j] = icasig[j][i];
	    	  }
	      }
	      
	      int k =0;
	      k = k+1;
		
		return IcaOut;
	}
	

	
}
