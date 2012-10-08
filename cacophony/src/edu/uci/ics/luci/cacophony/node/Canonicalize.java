package edu.uci.ics.luci.cacophony.node;

import java.util.HashSet;
import java.util.Set;

import weka.core.Capabilities;
import weka.core.Capabilities.Capability;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.RevisionUtils;
import weka.core.SparseInstance;
import weka.core.Utils;
import weka.filters.Sourcable;
import weka.filters.UnsupervisedFilter;
import weka.filters.unsupervised.attribute.PotentialClassIgnorer;

/** 
 <!-- globalinfo-start -->
 * Standardizes specified numeric attributes in the given dataset to have zero mean and unit variance (apart from the class attribute, if set).
 * <p/>
 <!-- globalinfo-end -->
 *
 <!-- options-start -->
 * Valid options are: <p/>
 * 
 * <pre> -unset-class-temporarily
 *  Unsets the class index temporarily before the filter is
 *  applied to the data.
 *  (default: no)</pre>
 * 
 <!-- options-end -->
 * 
 */
public class Canonicalize 
	extends PotentialClassIgnorer 
	implements UnsupervisedFilter, Sourcable{
  
	/** for serialization */
	private static final long serialVersionUID = -4781283536482814912L;

	/** The maxs **/
	private double [] m_Maxs;
	
	/** The mins **/
	private double [] m_Mins;
	
	/** The means */
	private double [] m_Means;
  
	/** The variances */
	private double [] m_StdDevs;
  
	
	/** The attribute indices that we are scaling: remapping to 0 - 1 **/
	private Set<Integer> scaledAttributes = new HashSet<Integer>();
	/** The attribute indices that we are standardizing **/
	private Set<Integer> standardizedAttributes = new HashSet<Integer>();

	/**
	 * Returns a string describing this filter
	 *
	 * @return a description of the filter suitable for
	 * displaying in the explorer/experimenter gui
	 */
	public String globalInfo() {
		return "Canonicalize the numeric attributes by optionally first rescaling them to span 0 to 1," +
				" then optionally adjusting so that it has zero mean and unit variance."+
				" Attributes have to be specifically set otherwise the filter has no impact on the data."+
				" No special handling for class attribute is done.  It is treated just like other attributes.";
	}

	/** 
	 * Returns the Capabilities of this filter.
	 *
	 * @return            the capabilities of this object
	 * @see               Capabilities
	 */
	public Capabilities getCapabilities() {
		Capabilities result = super.getCapabilities();
		result.disableAll();

		// attributes
		result.enableAllAttributes();
		result.enable(Capability.MISSING_VALUES);
    
		// class
		result.enableAllClasses();
		result.enable(Capability.MISSING_CLASS_VALUES);
		result.enable(Capability.NO_CLASS);
    
		return result;
	}
  
	void addScaledAttributed(Set<Integer> newOnes){
		this.scaledAttributes.addAll(newOnes);
	}
  
	void removeScaledAttributes(Set<Integer> notWanted){
		this.scaledAttributes.removeAll(notWanted);
	}
  
	void addStandardizedAttributed(Set<Integer> newOnes){
		this.standardizedAttributes.addAll(newOnes);
	}
  
	void removeRequestedAttributes(Set<Integer> notWanted){
		this.standardizedAttributes.removeAll(notWanted);
	}

	/**
	 * Sets the format of the input instances.
	 *
	 * @param instanceInfo an Instances object containing the input 
	 * instance structure (any instances contained in the object are 
	 * ignored - only the structure is required).
	 * @return true if the outputFormat may be collected immediately
	 * @throws Exception if the input format can't be set 
	 * successfully
	 */
	public boolean setInputFormat(Instances instanceInfo) throws Exception {

		super.setInputFormat(instanceInfo);
		setOutputFormat(instanceInfo);
		m_Mins = new double[instanceInfo.numAttributes()];
		m_Maxs = new double[instanceInfo.numAttributes()];
		m_Means = m_StdDevs = null;
		return true;
	}

	/**
	 * Input an instance for filtering. Filter requires all
	 * training instances be read before producing output.
	 *
	 * @param instance the input instance
	 * @return true if the filtered instance may now be
	 * collected with output().
	 * @throws IllegalStateException if no input format has been set.
	 */
	public boolean input(Instance instance) throws Exception {

		if (getInputFormat() == null) {
			throw new IllegalStateException("No input instance format defined");
		}
		if (m_NewBatch) {
			resetQueue();
			m_NewBatch = false;
		}
		if (m_Means == null) {
			bufferInput(instance);
			return false;
		} else {
			convertInstance(instance);
			return true;
		}
	}

	/**
	 * Signify that this batch of input to the filter is finished. 
	 * If the filter requires all instances prior to filtering,
	 * output() may now be called to retrieve the filtered instances.
	 *
	 * @return true if there are instances pending output
	 * @exception Exception if an error occurs
	 * @exception IllegalStateException if no input structure has been defined
	 */
	public boolean batchFinished() throws Exception {

		if (getInputFormat() == null) {
			throw new IllegalStateException("No input instance format defined");
		}
		if (m_Means == null) {
			Instances input = getInputFormat();
			m_Means = new double[input.numAttributes()];
			m_StdDevs = new double[input.numAttributes()];
			for (int i = 0; i < input.numAttributes(); i++) {
				if (input.attribute(i).isNumeric() &&
						(input.classIndex() != i)) {
					m_Means[i] = input.meanOrMode(i);
					m_StdDevs[i] = Math.sqrt(input.variance(i));
					m_Mins[i] = input.kthSmallestValue(i, 1);
					m_Maxs[i] = input.kthSmallestValue(i, input.numInstances());
				}
			}

			// Convert pending input instances
			for(int i = 0; i < input.numInstances(); i++) {
				convertInstance(input.instance(i));
			}
		} 
		// Free memory
		flushInput();

		m_NewBatch = true;
		return (numPendingOutput() != 0);
	}

	/**
	 * Convert a single instance over. The converted instance is 
	 * added to the end of the output queue.
	 *
	 * @param instance the instance to convert
	 * @exception Exception if an error occurs
	 */
	private void convertInstance(Instance instance) throws Exception {

		Instance inst = null;
		if (instance instanceof SparseInstance) {
			double[] newVals = new double[instance.numAttributes()];
			int[] newIndices = new int[instance.numAttributes()];
			double[] vals = instance.toDoubleArray();
			int ind = 0;
			for (int j = 0; j < instance.numAttributes(); j++) {
				double value = vals[j];
				if (instance.attribute(j).isNumeric() &&
						(!Instance.isMissingValue(value))){
				  
					value = toCanonical(j, value);
				  
					if (value != 0.0) {
						newVals[ind] = value;
						newIndices[ind] = j;
						ind++;
					}
				} else {
					if (value != 0.0) {
						newVals[ind] = value;
						newIndices[ind] = j;
						ind++;
					}
				}
			}	
			double[] tempVals = new double[ind];
			int[] tempInd = new int[ind];
			System.arraycopy(newVals, 0, tempVals, 0, ind);
			System.arraycopy(newIndices, 0, tempInd, 0, ind);
			inst = new SparseInstance(instance.weight(), tempVals, tempInd, instance.numAttributes());
		} else {
			double[] vals = instance.toDoubleArray();
			for (int j = 0; j < getInputFormat().numAttributes(); j++) {
				if (instance.attribute(j).isNumeric() &&
						(!Instance.isMissingValue(vals[j]))){
					vals[j] = toCanonical(j, vals[j]);
				}
			}	
			inst = new Instance(instance.weight(), vals);
		}
		inst.setDataset(instance.dataset());
		push(inst);
	}

	public double toCanonical(int j, double value) throws Exception {
	  
		if(scaledAttributes.contains(j)){
			value = (value-m_Mins[j]);
			if((m_Maxs[j]-m_Mins[j]) > 0.0){
				value = value /(m_Maxs[j]-m_Mins[j]);
			}
		}
	  
		if(standardizedAttributes.contains(j)){
			value = value - m_Means[j];
			// Just subtract the mean if the standard deviation is zero
			if (m_StdDevs[j] > 0) { 
				value = value / m_StdDevs[j];
			}
		}
	  
		if (Double.isNaN(value)) {
			throw new Exception("A NaN value was generated "
					+ "while standardizing attribute " 
					+ j);
		}
	  
		return value;
	}
	
	public double fromCanonical(int j, double value) throws Exception {
		
		if(this.standardizedAttributes.contains(j)){
			// Just subtract the mean if the standard deviation is zero
			if (m_StdDevs[j] > 0) { 
				value = (value * m_StdDevs[j]);
			} 
			value = value + m_Means[j];
		}
	  
		if(this.scaledAttributes.contains(j)){
			if((m_Maxs[j]-m_Mins[j]) > 0.0){
				value = value * (m_Maxs[j]-m_Mins[j]);
			}
			value = (value + m_Mins[j]);
		}
	  
		if (Double.isNaN(value)) {
			throw new Exception("A NaN value was generated "
					+ "while uncanonicalizing attribute " 
					+ j);
		}
	  
		return value;
	}
  
	/**
	 * Returns a string that describes the filter as source. The
	 * filter will be contained in a class with the given name (there may
	 * be auxiliary classes),
	 * and will contain two methods with these signatures:
	 * <pre><code>
	 * // converts one row
	 * public static Object[] filter(Object[] i);
	 * // converts a full dataset (first dimension is row index)
	 * public static Object[][] filter(Object[][] i);
	 * </code></pre>
	 * where the array <code>i</code> contains elements that are either
	 * Double, String, with missing values represented as null. The generated
	 * code is public domain and comes with no warranty.
	 *
	 * @param className   the name that should be given to the source class.
	 * @param data	the dataset used for initializing the filter
	 * @return            the object source described by a string
	 * @throws Exception  if the source can't be computed
	 */
	public String toSource(String className, Instances data) throws Exception {
		StringBuffer        result;
		boolean[]		process;
		int			i;

		result = new StringBuffer();
    
		// determine what attributes were processed
		process = new boolean[data.numAttributes()];
		for (i = 0; i < data.numAttributes(); i++) {
			process[i] = (data.attribute(i).isNumeric() && (this.scaledAttributes.contains(i) || this.standardizedAttributes.contains(i)));
		}
    
		result.append("class " + className + " {\n");
		result.append("\n");
		result.append("  /** lists which attributes will be processed */\n");
		result.append("  protected final static boolean[] PROCESS = new boolean[]{" + Utils.arrayToString(process) + "};\n");
		result.append("\n");
		result.append("  /** the computed means */\n");
		result.append("  protected final static double[] MEANS = new double[]{" + Utils.arrayToString(m_Means) + "};\n");
		result.append("\n");
		result.append("  /** the computed standard deviations */\n");
		result.append("  protected final static double[] STDEVS = new double[]{" + Utils.arrayToString(m_StdDevs) + "};\n");
		result.append("\n");
		result.append("  /**\n");
		result.append("  /** the computed mins */\n");
		result.append("  protected final static double[] MINS = new double[]{" + Utils.arrayToString(m_Mins) + "};\n");
		result.append("\n");
		result.append("  /**\n");
		result.append("  /** the computed maxs */\n");
		result.append("  protected final static double[] MAXS = new double[]{" + Utils.arrayToString(m_Maxs) + "};\n");
		result.append("\n");
		result.append("  /**\n");
		result.append("   * filters a single row\n");
		result.append("   * \n");
		result.append("   * @param i the row to process\n");
		result.append("   * @return the processed row\n");
		result.append("   */\n");
		result.append("   This needs to be fixed to reflect the code");
		result.append("  public static Object[] filter(Object[] i) {\n");
		result.append("    Object[] result;\n");
		result.append("\n");
		result.append("    result = new Object[i.length];\n");
		result.append("    for (int n = 0; n < i.length; n++) {\n");
		result.append("      if (PROCESS[n] && (i[n] != null)) {\n");
		result.append("        if (STDEVS[n] > 0)\n");
		result.append("          result[n] = (((Double) i[n]) - MEANS[n]) / STDEVS[n];\n");
		result.append("        else\n");
		result.append("          result[n] = ((Double) i[n]) - MEANS[n];\n");
		result.append("      }\n");
		result.append("      else {\n");
		result.append("        result[n] = i[n];\n");
		result.append("      }\n");
		result.append("    }\n");
		result.append("\n");
		result.append("    return result;\n");
		result.append("  }\n");
		result.append("\n");
		result.append("  /**\n");
		result.append("   * filters multiple rows\n");
		result.append("   * \n");
		result.append("   * @param i the rows to process\n");
		result.append("   * @return the processed rows\n");
		result.append("   */\n");
		result.append("  public static Object[][] filter(Object[][] i) {\n");
		result.append("    Object[][] result;\n");
		result.append("\n");
		result.append("    result = new Object[i.length][];\n");
		result.append("    for (int n = 0; n < i.length; n++) {\n");
		result.append("      result[n] = filter(i[n]);\n");
		result.append("    }\n");
		result.append("\n");
		result.append("    return result;\n");
		result.append("  }\n");
		result.append("}\n");
    
		return result.toString();
	}

	/**
	 * Returns the revision string.
	 * 
	 * @return		the revision
	 */
	public String getRevision() {
		return RevisionUtils.extract("$Revision: 5547 $");
	}

	/**
	 * Main method for testing this class.
	 *
	 * @param argv should contain arguments to the filter: 
	 * use -h for help
	 */
	public static void main(String [] argv) {
		runFilter(new Canonicalize(), argv);
	}
}
